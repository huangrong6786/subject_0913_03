package com.evops.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.evops.common.BizAudits;
import com.evops.dto.VolumeCreateRequest;
import com.evops.dto.VolumeTraceResponse;
import com.evops.entity.AcceptRecord;
import com.evops.entity.ArchiveVolume;
import com.evops.entity.BatchVolume;
import com.evops.entity.InspectSample;
import com.evops.entity.RestoreOrder;
import com.evops.enums.VolumeStatus;
import com.evops.mapper.AcceptRecordMapper;
import com.evops.mapper.ArchiveVolumeMapper;
import com.evops.mapper.BatchVolumeMapper;
import com.evops.mapper.InspectSampleMapper;
import com.evops.mapper.RestoreOrderMapper;
import com.evops.service.ArchiveVolumeService;
import com.evops.service.support.RestoreSnapshots;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class ArchiveVolumeServiceImpl implements ArchiveVolumeService {
    private final ArchiveVolumeMapper volumeMapper;
    private final RestoreOrderMapper orderMapper;
    private final InspectSampleMapper sampleMapper;
    private final AcceptRecordMapper acceptRecordMapper;
    private final BatchVolumeMapper batchVolumeMapper;

    public ArchiveVolumeServiceImpl(ArchiveVolumeMapper volumeMapper,
                                    RestoreOrderMapper orderMapper,
                                    InspectSampleMapper sampleMapper,
                                    AcceptRecordMapper acceptRecordMapper,
                                    BatchVolumeMapper batchVolumeMapper) {
        this.volumeMapper = volumeMapper;
        this.orderMapper = orderMapper;
        this.sampleMapper = sampleMapper;
        this.acceptRecordMapper = acceptRecordMapper;
        this.batchVolumeMapper = batchVolumeMapper;
    }

    @Override
    @Transactional
    public ArchiveVolume register(VolumeCreateRequest request) {
        BizAudits.requireValidTimezone(request.getBizTimezone());
        String volumeNo = request.getVolumeNo().trim();
        String restoreVersion = request.getRestoreVersion().trim();
        Long count = volumeMapper.selectCount(Wrappers.<ArchiveVolume>lambdaQuery()
                .eq(ArchiveVolume::getVolumeNo, volumeNo)
                .eq(ArchiveVolume::getRestoreVersion, restoreVersion));
        if (count != null && count > 0) {
            throw new IllegalArgumentException("馆藏册号与修复版本组合已存在");
        }
        ArchiveVolume volume = new ArchiveVolume();
        volume.setVolumeNo(volumeNo);
        volume.setRestoreVersion(restoreVersion);
        volume.setTitle(request.getTitle().trim());
        volume.setPaperType(request.getPaperType());
        volume.setStatus(VolumeStatus.REGISTERED.name());
        volume.setMoistureContent(request.getMoistureContent());
        volume.setPhValue(request.getPhValue());
        volume.setFiberStrength(request.getFiberStrength());
        volume.setReceivedDate(request.getReceivedDate());
        volume.setRequestNo(BizAudits.newRequestNo());
        volume.setOperator(request.getOperator().trim());
        volume.setBizTimezone(request.getBizTimezone().trim());
        volume.setVersionSnapshot(RestoreSnapshots.volumeVersion(volume));
        try {
            volumeMapper.insert(volume);
        } catch (DuplicateKeyException ex) {
            // 并发下依赖唯一约束兜底，保证馆藏册号+修复版本唯一
            throw new IllegalArgumentException("馆藏册号与修复版本组合已存在");
        }
        return volume;
    }

    @Override
    public List<ArchiveVolume> list(String keyword, String status) {
        if (StringUtils.hasText(status) && !VolumeStatus.isValid(status)) {
            throw new IllegalArgumentException("无效的馆藏册状态: " + status);
        }
        return volumeMapper.selectList(Wrappers.<ArchiveVolume>lambdaQuery()
                .and(StringUtils.hasText(keyword), q -> q
                        .like(ArchiveVolume::getVolumeNo, keyword)
                        .or()
                        .like(ArchiveVolume::getTitle, keyword))
                .eq(StringUtils.hasText(status), ArchiveVolume::getStatus, status)
                .orderByAsc(ArchiveVolume::getId));
    }

    @Override
    public ArchiveVolume getById(Long id) {
        ArchiveVolume volume = volumeMapper.selectById(id);
        if (volume == null) {
            throw new IllegalArgumentException("馆藏册不存在");
        }
        return volume;
    }

    @Override
    public VolumeTraceResponse trace(Long id) {
        ArchiveVolume volume = getById(id);
        List<RestoreOrder> orders = orderMapper.selectList(Wrappers.<RestoreOrder>lambdaQuery()
                .eq(RestoreOrder::getVolumeId, id)
                .orderByAsc(RestoreOrder::getId));
        List<VolumeTraceResponse.OrderTrace> traces = new ArrayList<>();
        for (RestoreOrder order : orders) {
            List<InspectSample> samples = sampleMapper.selectList(Wrappers.<InspectSample>lambdaQuery()
                    .eq(InspectSample::getOrderId, order.getId())
                    .orderByAsc(InspectSample::getId));
            List<AcceptRecord> accepts = acceptRecordMapper.selectList(Wrappers.<AcceptRecord>lambdaQuery()
                    .eq(AcceptRecord::getOrderId, order.getId())
                    .orderByAsc(AcceptRecord::getId));
            traces.add(new VolumeTraceResponse.OrderTrace(order, samples, accepts));
        }
        return new VolumeTraceResponse(volume, traces);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ArchiveVolume volume = getById(id);
        if (VolumeStatus.ACCEPTED.name().equals(volume.getStatus())
                || VolumeStatus.POSTED.name().equals(volume.getStatus())) {
            throw new IllegalStateException("已验收或已落账的馆藏册不能直接删除");
        }
        Long orderCount = orderMapper.selectCount(Wrappers.<RestoreOrder>lambdaQuery()
                .eq(RestoreOrder::getVolumeId, id));
        if (orderCount != null && orderCount > 0) {
            throw new IllegalStateException("馆藏册已生成修复工单，不能直接删除");
        }
        batchVolumeMapper.delete(Wrappers.<BatchVolume>lambdaQuery()
                .eq(BatchVolume::getVolumeId, id));
        volumeMapper.deleteById(id);
    }
}
