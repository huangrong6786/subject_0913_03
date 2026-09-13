package com.evops.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.evops.common.BizAudits;
import com.evops.common.Snapshots;
import com.evops.dto.OrderCreateRequest;
import com.evops.dto.OrderTransitionRequest;
import com.evops.entity.AcceptRecord;
import com.evops.entity.ArchiveVolume;
import com.evops.entity.BatchVolume;
import com.evops.entity.InspectSample;
import com.evops.entity.RestoreBatch;
import com.evops.entity.RestoreOrder;
import com.evops.enums.OrderStatus;
import com.evops.enums.VolumeStatus;
import com.evops.mapper.AcceptRecordMapper;
import com.evops.mapper.ArchiveVolumeMapper;
import com.evops.mapper.BatchVolumeMapper;
import com.evops.mapper.InspectSampleMapper;
import com.evops.mapper.RestoreBatchMapper;
import com.evops.mapper.RestoreOrderMapper;
import com.evops.service.RestoreOrderService;
import com.evops.service.support.RestoreSnapshots;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RestoreOrderServiceImpl implements RestoreOrderService {
    private final RestoreOrderMapper orderMapper;
    private final RestoreBatchMapper batchMapper;
    private final ArchiveVolumeMapper volumeMapper;
    private final BatchVolumeMapper batchVolumeMapper;
    private final InspectSampleMapper sampleMapper;
    private final AcceptRecordMapper acceptRecordMapper;

    public RestoreOrderServiceImpl(RestoreOrderMapper orderMapper,
                                   RestoreBatchMapper batchMapper,
                                   ArchiveVolumeMapper volumeMapper,
                                   BatchVolumeMapper batchVolumeMapper,
                                   InspectSampleMapper sampleMapper,
                                   AcceptRecordMapper acceptRecordMapper) {
        this.orderMapper = orderMapper;
        this.batchMapper = batchMapper;
        this.volumeMapper = volumeMapper;
        this.batchVolumeMapper = batchVolumeMapper;
        this.sampleMapper = sampleMapper;
        this.acceptRecordMapper = acceptRecordMapper;
    }

    @Override
    @Transactional
    public RestoreOrder create(OrderCreateRequest request) {
        BizAudits.requireValidTimezone(request.getBizTimezone());
        RestoreBatch batch = batchMapper.selectById(request.getBatchId());
        if (batch == null) {
            throw new IllegalArgumentException("修复批次不存在");
        }
        ArchiveVolume volume = volumeMapper.selectById(request.getVolumeId());
        if (volume == null) {
            throw new IllegalArgumentException("馆藏册不存在");
        }
        Long inBatch = batchVolumeMapper.selectCount(Wrappers.<BatchVolume>lambdaQuery()
                .eq(BatchVolume::getBatchId, batch.getId())
                .eq(BatchVolume::getVolumeId, volume.getId()));
        if (inBatch == null || inBatch == 0) {
            throw new IllegalArgumentException("馆藏册未加入该批次");
        }
        String orderNo = request.getOrderNo().trim();
        Long count = orderMapper.selectCount(Wrappers.<RestoreOrder>lambdaQuery()
                .eq(RestoreOrder::getOrderNo, orderNo));
        if (count != null && count > 0) {
            throw new IllegalArgumentException("工单号已存在");
        }
        // 条件更新保证同一馆藏册不会并发建立多张工单
        LocalDateTime now = LocalDateTime.now();
        int updated = volumeMapper.update(null, Wrappers.<ArchiveVolume>lambdaUpdate()
                .set(ArchiveVolume::getStatus, VolumeStatus.RESTORING.name())
                .set(ArchiveVolume::getUpdateTime, now)
                .eq(ArchiveVolume::getId, volume.getId())
                .eq(ArchiveVolume::getStatus, VolumeStatus.BATCHED.name()));
        if (updated == 0) {
            throw new IllegalStateException("馆藏册当前状态为"
                    + VolumeStatus.descriptionOf(volume.getStatus()) + "，不能建立修复工单");
        }
        RestoreOrder order = new RestoreOrder();
        order.setOrderNo(orderNo);
        order.setBatchId(batch.getId());
        order.setVolumeId(volume.getId());
        order.setProcessName(request.getProcessName().trim());
        order.setStatus(OrderStatus.CREATED.name());
        // 工序载体落库时保留修复前纸张状态快照
        order.setPaperSnapshot(Snapshots.paper(volume.getMoistureContent(),
                volume.getPhValue(), volume.getFiberStrength()));
        order.setRequestNo(BizAudits.newRequestNo());
        order.setOperator(request.getOperator().trim());
        order.setBizTimezone(request.getBizTimezone().trim());
        volume.setStatus(VolumeStatus.RESTORING.name());
        order.setVersionSnapshot(RestoreSnapshots.volumeVersion(volume));
        try {
            orderMapper.insert(order);
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("工单号已存在");
        }
        return order;
    }

    @Override
    @Transactional
    public RestoreOrder transition(Long id, OrderTransitionRequest request) {
        BizAudits.requireValidTimezone(request.getBizTimezone());
        RestoreOrder order = getById(id);
        String action = request.getAction().trim().toUpperCase();
        LocalDateTime now = LocalDateTime.now();
        if ("START".equals(action)) {
            // 条件更新保证工单只被开工一次
            int updated = orderMapper.update(null, Wrappers.<RestoreOrder>lambdaUpdate()
                    .set(RestoreOrder::getStatus, OrderStatus.IN_PROGRESS.name())
                    .set(RestoreOrder::getStartDate, now.toLocalDate())
                    .set(RestoreOrder::getUpdateTime, now)
                    .eq(RestoreOrder::getId, id)
                    .eq(RestoreOrder::getStatus, OrderStatus.CREATED.name()));
            if (updated == 0) {
                throw new IllegalStateException("工单当前状态为"
                        + OrderStatus.descriptionOf(order.getStatus()) + "，不能开工");
            }
        } else if ("COMPLETE".equals(action)) {
            if (request.getWorkHours() == null) {
                throw new IllegalArgumentException("完工登记必须填写工时");
            }
            if (request.getMoistureContent() == null
                    || request.getPhValue() == null
                    || request.getFiberStrength() == null) {
                throw new IllegalArgumentException("完工登记必须填写纸张状态（含水率、酸碱度、纤维强度）");
            }
            String paperSnapshot = Snapshots.paper(request.getMoistureContent(),
                    request.getPhValue(), request.getFiberStrength());
            // 条件更新保证工单只被完工一次，完工时保留工序纸张状态快照
            int updated = orderMapper.update(null, Wrappers.<RestoreOrder>lambdaUpdate()
                    .set(RestoreOrder::getStatus, OrderStatus.COMPLETED.name())
                    .set(RestoreOrder::getFinishDate, now.toLocalDate())
                    .set(RestoreOrder::getWorkHours, request.getWorkHours())
                    .set(RestoreOrder::getPaperSnapshot, paperSnapshot)
                    .set(RestoreOrder::getUpdateTime, now)
                    .eq(RestoreOrder::getId, id)
                    .eq(RestoreOrder::getStatus, OrderStatus.IN_PROGRESS.name()));
            if (updated == 0) {
                throw new IllegalStateException("工单当前状态为"
                        + OrderStatus.descriptionOf(order.getStatus()) + "，不能完工");
            }
            // 同一事务同步馆藏册当前纸张状态
            volumeMapper.update(null, Wrappers.<ArchiveVolume>lambdaUpdate()
                    .set(ArchiveVolume::getMoistureContent, request.getMoistureContent())
                    .set(ArchiveVolume::getPhValue, request.getPhValue())
                    .set(ArchiveVolume::getFiberStrength, request.getFiberStrength())
                    .set(ArchiveVolume::getUpdateTime, now)
                    .eq(ArchiveVolume::getId, order.getVolumeId()));
        } else {
            throw new IllegalArgumentException("无效的流转动作: " + action + "，仅支持 START/COMPLETE");
        }
        return getById(id);
    }

    @Override
    public List<RestoreOrder> list(Long batchId, Long volumeId, String status) {
        if (StringUtils.hasText(status) && !OrderStatus.isValid(status)) {
            throw new IllegalArgumentException("无效的工单状态: " + status);
        }
        return orderMapper.selectList(Wrappers.<RestoreOrder>lambdaQuery()
                .eq(batchId != null, RestoreOrder::getBatchId, batchId)
                .eq(volumeId != null, RestoreOrder::getVolumeId, volumeId)
                .eq(StringUtils.hasText(status), RestoreOrder::getStatus, status)
                .orderByAsc(RestoreOrder::getId));
    }

    @Override
    public RestoreOrder getById(Long id) {
        RestoreOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new IllegalArgumentException("修复工单不存在");
        }
        return order;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        RestoreOrder order = getById(id);
        if (OrderStatus.ACCEPTED.name().equals(order.getStatus())) {
            throw new IllegalStateException("已验收的修复工单不能直接删除");
        }
        Long sampleCount = sampleMapper.selectCount(Wrappers.<InspectSample>lambdaQuery()
                .eq(InspectSample::getOrderId, id));
        if (sampleCount != null && sampleCount > 0) {
            throw new IllegalStateException("工单已登记检测样本，不能直接删除");
        }
        Long acceptCount = acceptRecordMapper.selectCount(Wrappers.<AcceptRecord>lambdaQuery()
                .eq(AcceptRecord::getOrderId, id));
        if (acceptCount != null && acceptCount > 0) {
            throw new IllegalStateException("工单已存在验收记录，不能直接删除");
        }
        orderMapper.deleteById(id);
        // 同一事务内：馆藏册无其他工单时回退为已入批
        Long remaining = orderMapper.selectCount(Wrappers.<RestoreOrder>lambdaQuery()
                .eq(RestoreOrder::getVolumeId, order.getVolumeId()));
        if (remaining == null || remaining == 0) {
            volumeMapper.update(null, Wrappers.<ArchiveVolume>lambdaUpdate()
                    .set(ArchiveVolume::getStatus, VolumeStatus.BATCHED.name())
                    .set(ArchiveVolume::getUpdateTime, LocalDateTime.now())
                    .eq(ArchiveVolume::getId, order.getVolumeId())
                    .eq(ArchiveVolume::getStatus, VolumeStatus.RESTORING.name()));
        }
    }
}
