package com.evops;

import com.evops.dto.AcceptCreateRequest;
import com.evops.dto.BatchCreateRequest;
import com.evops.dto.OrderCreateRequest;
import com.evops.dto.OrderTransitionRequest;
import com.evops.dto.VolumeCreateRequest;
import com.evops.entity.AcceptRecord;
import com.evops.entity.ArchiveVolume;
import com.evops.entity.RestoreBatch;
import com.evops.entity.RestoreOrder;
import com.evops.enums.OrderStatus;
import com.evops.enums.VolumeStatus;
import com.evops.service.AcceptRecordService;
import com.evops.service.ArchiveVolumeService;
import com.evops.service.RestoreBatchService;
import com.evops.service.RestoreOrderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 困难级并发约束验证：5 个并发写入请求下，
 * 业务键唯一与状态流转只能成功一次，跨表写入保持事务一致。
 */
@SpringBootTest
public class RestoreConcurrencyTest {
    private static final int CONCURRENCY = 5;

    @Autowired
    private ArchiveVolumeService volumeService;
    @Autowired
    private RestoreBatchService batchService;
    @Autowired
    private RestoreOrderService orderService;
    @Autowired
    private AcceptRecordService acceptRecordService;

    @Test
    public void concurrentVolumeRegistrationKeepsBusinessKeyUnique() throws Exception {
        List<Boolean> results = runConcurrent(CONCURRENCY, () -> {
            try {
                volumeService.register(volumeRequest("VOL-CONC-001", "V1"));
                return true;
            } catch (RuntimeException ex) {
                return false;
            }
        });
        Assertions.assertEquals(1, successCount(results), "同一馆藏册号+修复版本并发登记只能成功一次");
        Assertions.assertEquals(1, volumeService.list("VOL-CONC-001", null).size());
    }

    @Test
    public void concurrentBatchCreationKeepsBatchNoUnique() throws Exception {
        ArchiveVolume volume = volumeService.register(volumeRequest("VOL-CONC-002", "V1"));
        List<Boolean> results = runConcurrent(CONCURRENCY, () -> {
            try {
                BatchCreateRequest request = new BatchCreateRequest();
                request.setBatchNo("BATCH-CONC-001");
                request.setBatchName("并发批次");
                request.setBizDate(LocalDate.of(2026, 9, 13));
                request.setVolumeIds(Collections.singletonList(volume.getId()));
                request.setOperator("tester");
                request.setBizTimezone("Asia/Shanghai");
                batchService.create(request);
                return true;
            } catch (RuntimeException ex) {
                return false;
            }
        });
        Assertions.assertEquals(1, successCount(results), "同一批次号并发建立只能成功一次");
        // 批次建立是跨表事务：失败方整体回滚，最终只落库一个批次，馆藏册只入批一次
        Assertions.assertEquals(1, batchService.list().stream()
                .filter(b -> "BATCH-CONC-001".equals(b.getBatchNo())).count());
        ArchiveVolume reloaded = volumeService.getById(volume.getId());
        Assertions.assertEquals(VolumeStatus.BATCHED.name(), reloaded.getStatus());
    }

    @Test
    public void concurrentAcceptOnlyOneWins() throws Exception {
        ArchiveVolume volume = volumeService.register(volumeRequest("VOL-CONC-003", "V1"));
        RestoreBatch batch = batchService.create(batchRequest("BATCH-CONC-002", volume.getId()));
        RestoreOrder order = orderService.create(orderRequest("RO-CONC-001", batch.getId(), volume.getId()));
        orderService.transition(order.getId(), transitionRequest("START"));
        orderService.transition(order.getId(), completeRequest());

        List<Boolean> results = runConcurrent(CONCURRENCY, () -> {
            try {
                AcceptCreateRequest request = acceptRequest(
                        "ACC-CONC-" + System.nanoTime(), order.getId());
                acceptRecordService.create(request);
                return true;
            } catch (RuntimeException ex) {
                return false;
            }
        });
        Assertions.assertEquals(1, successCount(results), "同一工单并发验收只能成功一次");
        Assertions.assertEquals(1, acceptRecordService.list(order.getId(), null).size());
        Assertions.assertEquals(OrderStatus.ACCEPTED.name(),
                orderService.getById(order.getId()).getStatus());
        Assertions.assertEquals(VolumeStatus.ACCEPTED.name(),
                volumeService.getById(volume.getId()).getStatus());
    }

    @Test
    public void acceptedAndPostedRecordsCannotBeDeleted() {
        ArchiveVolume volume = volumeService.register(volumeRequest("VOL-CONC-004", "V1"));
        RestoreBatch batch = batchService.create(batchRequest("BATCH-CONC-003", volume.getId()));
        RestoreOrder order = orderService.create(orderRequest("RO-CONC-002", batch.getId(), volume.getId()));
        orderService.transition(order.getId(), transitionRequest("START"));
        orderService.transition(order.getId(), completeRequest());
        AcceptCreateRequest acceptRequest = acceptRequest("ACC-CONC-900", order.getId());
        acceptRequest.setIssueReport(true);
        AcceptRecord accept = acceptRecordService.create(acceptRequest);

        // 已落账馆藏册不能删除
        Assertions.assertThrows(IllegalStateException.class,
                () -> volumeService.delete(volume.getId()));
        // 已验收工单不能删除
        Assertions.assertThrows(IllegalStateException.class,
                () -> orderService.delete(order.getId()));
        // 已签发报告不能删除
        Assertions.assertThrows(IllegalStateException.class,
                () -> acceptRecordService.delete(accept.getId()));
        Assertions.assertEquals(VolumeStatus.POSTED.name(),
                volumeService.getById(volume.getId()).getStatus());
        Assertions.assertEquals(Boolean.TRUE,
                acceptRecordService.getById(accept.getId()).getReportIssued());
    }

    private int successCount(List<Boolean> results) {
        int success = 0;
        for (Boolean result : results) {
            if (Boolean.TRUE.equals(result)) {
                success++;
            }
        }
        return success;
    }

    private List<Boolean> runConcurrent(int threads, ThrowingTask task) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                ready.countDown();
                go.await();
                return task.run();
            }));
        }
        Assertions.assertTrue(ready.await(10, TimeUnit.SECONDS));
        go.countDown();
        List<Boolean> results = new ArrayList<>();
        for (Future<Boolean> future : futures) {
            results.add(future.get(30, TimeUnit.SECONDS));
        }
        pool.shutdown();
        return results;
    }

    private interface ThrowingTask {
        Boolean run() throws Exception;
    }

    private VolumeCreateRequest volumeRequest(String volumeNo, String restoreVersion) {
        VolumeCreateRequest request = new VolumeCreateRequest();
        request.setVolumeNo(volumeNo);
        request.setRestoreVersion(restoreVersion);
        request.setTitle("宋版《资治通鉴》残卷");
        request.setPaperType("竹纸");
        request.setReceivedDate(LocalDate.of(2026, 9, 1));
        request.setMoistureContent(new BigDecimal("12.50"));
        request.setPhValue(new BigDecimal("5.80"));
        request.setFiberStrength(new BigDecimal("320.00"));
        request.setOperator("tester");
        request.setBizTimezone("Asia/Shanghai");
        return request;
    }

    private BatchCreateRequest batchRequest(String batchNo, Long volumeId) {
        BatchCreateRequest request = new BatchCreateRequest();
        request.setBatchNo(batchNo);
        request.setBatchName("秋季修复批次");
        request.setBizDate(LocalDate.of(2026, 9, 13));
        request.setVolumeIds(Collections.singletonList(volumeId));
        request.setOperator("tester");
        request.setBizTimezone("Asia/Shanghai");
        return request;
    }

    private OrderCreateRequest orderRequest(String orderNo, Long batchId, Long volumeId) {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setOrderNo(orderNo);
        request.setBatchId(batchId);
        request.setVolumeId(volumeId);
        request.setProcessName("去污补纸");
        request.setOperator("tester");
        request.setBizTimezone("Asia/Shanghai");
        return request;
    }

    private OrderTransitionRequest transitionRequest(String action) {
        OrderTransitionRequest request = new OrderTransitionRequest();
        request.setAction(action);
        request.setOperator("tester");
        request.setBizTimezone("Asia/Shanghai");
        return request;
    }

    private OrderTransitionRequest completeRequest() {
        OrderTransitionRequest request = transitionRequest("COMPLETE");
        request.setWorkHours(new BigDecimal("6.5"));
        request.setMoistureContent(new BigDecimal("8.20"));
        request.setPhValue(new BigDecimal("6.90"));
        request.setFiberStrength(new BigDecimal("410.00"));
        return request;
    }

    private AcceptCreateRequest acceptRequest(String acceptNo, Long orderId) {
        AcceptCreateRequest request = new AcceptCreateRequest();
        request.setAcceptNo(acceptNo);
        request.setOrderId(orderId);
        request.setAcceptDate(LocalDate.of(2026, 9, 13));
        request.setResult("PASSED");
        request.setMoistureContent(new BigDecimal("8.00"));
        request.setPhValue(new BigDecimal("7.00"));
        request.setFiberStrength(new BigDecimal("420.00"));
        request.setOperator("tester");
        request.setBizTimezone("Asia/Shanghai");
        return request;
    }
}
