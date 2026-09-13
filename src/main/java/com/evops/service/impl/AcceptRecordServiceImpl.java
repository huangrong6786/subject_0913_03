package com.evops.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.evops.common.BizAudits;
import com.evops.common.Snapshots;
import com.evops.dto.AcceptCreateRequest;
import com.evops.entity.AcceptRecord;
import com.evops.entity.ArchiveVolume;
import com.evops.entity.RestoreOrder;
import com.evops.enums.AcceptResult;
import com.evops.enums.OrderStatus;
import com.evops.enums.VolumeStatus;
import com.evops.mapper.AcceptRecordMapper;
import com.evops.mapper.ArchiveVolumeMapper;
import com.evops.mapper.RestoreOrderMapper;
import com.evops.service.AcceptRecordService;
import com.evops.service.support.RestoreSnapshots;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AcceptRecordServiceImpl implements AcceptRecordService {
    private final AcceptRecordMapper acceptRecordMapper;
    private final RestoreOrderMapper orderMapper;
    private final ArchiveVolumeMapper volumeMapper;

    public AcceptRecordServiceImpl(AcceptRecordMapper acceptRecordMapper,
                                   RestoreOrderMapper orderMapper,
                                   ArchiveVolumeMapper volumeMapper) {
        this.acceptRecordMapper = acceptRecordMapper;
        this.orderMapper = orderMapper;
        this.volumeMapper = volumeMapper;
    }

    @Override
    @Transactional
    public AcceptRecord create(AcceptCreateRequest request) {
        BizAudits.requireValidTimezone(request.getBizTimezone());
        String result = request.getResult().trim().toUpperCase();
        if (!AcceptResult.isValid(result)) {
            throw new IllegalArgumentException("无效的验收结论: " + result + "，仅支持 PASSED/REJECTED");
        }
        boolean passed = AcceptResult.PASSED.name().equals(result);
        if (!passed && request.isIssueReport()) {
            throw new IllegalArgumentException("验收不通过不能签发报告");
        }
        RestoreOrder order = orderMapper.selectById(request.getOrderId());
        if (order == null) {
            throw new IllegalArgumentException("修复工单不存在");
        }
        ArchiveVolume volume = volumeMapper.selectById(order.getVolumeId());
        if (volume == null) {
            throw new IllegalStateException("工单关联的馆藏册不存在");
        }
        String acceptNo = request.getAcceptNo().trim();
        Long count = acceptRecordMapper.selectCount(Wrappers.<AcceptRecord>lambdaQuery()
                .eq(AcceptRecord::getAcceptNo, acceptNo));
        if (count != null && count > 0) {
            throw new IllegalArgumentException("验收单号已存在");
        }
        LocalDateTime now = LocalDateTime.now();
        // 条件更新保证同一工单不会被并发重复验收：通过则完工->已验收，不通过则退回修复中
        int orderUpdated = orderMapper.update(null, Wrappers.<RestoreOrder>lambdaUpdate()
                .set(RestoreOrder::getStatus, passed
                        ? OrderStatus.ACCEPTED.name() : OrderStatus.IN_PROGRESS.name())
                .set(RestoreOrder::getUpdateTime, now)
                .eq(RestoreOrder::getId, order.getId())
                .eq(RestoreOrder::getStatus, OrderStatus.COMPLETED.name()));
        if (orderUpdated == 0) {
            throw new IllegalStateException("工单当前状态为"
                    + OrderStatus.descriptionOf(order.getStatus()) + "，不能验收");
        }
        AcceptRecord record = new AcceptRecord();
        record.setAcceptNo(acceptNo);
        record.setOrderId(order.getId());
        record.setVolumeId(volume.getId());
        record.setResult(result);
        record.setAcceptDate(request.getAcceptDate());
        record.setReportIssued(passed && request.isIssueReport());
        // 验收落库时保留纸张状态快照
        record.setPaperSnapshot(Snapshots.paper(request.getMoistureContent(),
                request.getPhValue(), request.getFiberStrength()));
        record.setRequestNo(BizAudits.newRequestNo());
        record.setOperator(request.getOperator().trim());
        record.setBizTimezone(request.getBizTimezone().trim());
        String targetStatus = null;
        if (passed) {
            targetStatus = Boolean.TRUE.equals(record.getReportIssued())
                    ? VolumeStatus.POSTED.name() : VolumeStatus.ACCEPTED.name();
            volume.setStatus(targetStatus);
            volume.setMoistureContent(request.getMoistureContent());
            volume.setPhValue(request.getPhValue());
            volume.setFiberStrength(request.getFiberStrength());
        }
        record.setVersionSnapshot(RestoreSnapshots.volumeVersion(volume));
        try {
            acceptRecordMapper.insert(record);
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("验收单号已存在");
        }
        // 同一事务内流转馆藏册状态并同步验收实测纸张状态
        if (passed) {
            int volumeUpdated = volumeMapper.update(null, Wrappers.<ArchiveVolume>lambdaUpdate()
                    .set(ArchiveVolume::getStatus, targetStatus)
                    .set(ArchiveVolume::getMoistureContent, request.getMoistureContent())
                    .set(ArchiveVolume::getPhValue, request.getPhValue())
                    .set(ArchiveVolume::getFiberStrength, request.getFiberStrength())
                    .set(ArchiveVolume::getUpdateTime, now)
                    .eq(ArchiveVolume::getId, volume.getId())
                    .eq(ArchiveVolume::getStatus, VolumeStatus.RESTORING.name()));
            if (volumeUpdated == 0) {
                throw new IllegalStateException("馆藏册状态异常，无法完成验收");
            }
        }
        return record;
    }

    @Override
    @Transactional
    public AcceptRecord issueReport(Long id) {
        AcceptRecord record = getById(id);
        if (!AcceptResult.PASSED.name().equals(record.getResult())) {
            throw new IllegalStateException("仅验收通过的记录可签发报告");
        }
        LocalDateTime now = LocalDateTime.now();
        // 条件更新保证报告只签发一次；签发即落账
        int updated = acceptRecordMapper.update(null, Wrappers.<AcceptRecord>lambdaUpdate()
                .set(AcceptRecord::getReportIssued, true)
                .set(AcceptRecord::getUpdateTime, now)
                .eq(AcceptRecord::getId, id)
                .eq(AcceptRecord::getReportIssued, false)
                .eq(AcceptRecord::getResult, AcceptResult.PASSED.name()));
        if (updated == 0) {
            throw new IllegalStateException("报告已签发，不能重复签发");
        }
        int volumeUpdated = volumeMapper.update(null, Wrappers.<ArchiveVolume>lambdaUpdate()
                .set(ArchiveVolume::getStatus, VolumeStatus.POSTED.name())
                .set(ArchiveVolume::getUpdateTime, now)
                .eq(ArchiveVolume::getId, record.getVolumeId())
                .eq(ArchiveVolume::getStatus, VolumeStatus.ACCEPTED.name()));
        if (volumeUpdated == 0) {
            throw new IllegalStateException("馆藏册状态异常，无法落账");
        }
        return getById(id);
    }

    @Override
    public List<AcceptRecord> list(Long orderId, String result) {
        if (StringUtils.hasText(result) && !AcceptResult.isValid(result)) {
            throw new IllegalArgumentException("无效的验收结论: " + result);
        }
        return acceptRecordMapper.selectList(Wrappers.<AcceptRecord>lambdaQuery()
                .eq(orderId != null, AcceptRecord::getOrderId, orderId)
                .eq(StringUtils.hasText(result), AcceptRecord::getResult, result)
                .orderByAsc(AcceptRecord::getId));
    }

    @Override
    public AcceptRecord getById(Long id) {
        AcceptRecord record = acceptRecordMapper.selectById(id);
        if (record == null) {
            throw new IllegalArgumentException("验收记录不存在");
        }
        return record;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        AcceptRecord record = getById(id);
        if (Boolean.TRUE.equals(record.getReportIssued())) {
            throw new IllegalStateException("已签发的验收报告不能直接删除");
        }
        if (AcceptResult.PASSED.name().equals(record.getResult())) {
            throw new IllegalStateException("已验收的记录不能直接删除");
        }
        acceptRecordMapper.deleteById(id);
    }
}
