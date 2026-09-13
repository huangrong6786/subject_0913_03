package com.evops.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.evops.common.BizAudits;
import com.evops.common.Snapshots;
import com.evops.dto.SampleCreateRequest;
import com.evops.entity.ArchiveVolume;
import com.evops.entity.InspectSample;
import com.evops.entity.RestoreOrder;
import com.evops.enums.OrderStatus;
import com.evops.mapper.ArchiveVolumeMapper;
import com.evops.mapper.InspectSampleMapper;
import com.evops.mapper.RestoreOrderMapper;
import com.evops.service.InspectSampleService;
import com.evops.service.support.RestoreSnapshots;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InspectSampleServiceImpl implements InspectSampleService {
    private final InspectSampleMapper sampleMapper;
    private final RestoreOrderMapper orderMapper;
    private final ArchiveVolumeMapper volumeMapper;

    public InspectSampleServiceImpl(InspectSampleMapper sampleMapper,
                                    RestoreOrderMapper orderMapper,
                                    ArchiveVolumeMapper volumeMapper) {
        this.sampleMapper = sampleMapper;
        this.orderMapper = orderMapper;
        this.volumeMapper = volumeMapper;
    }

    @Override
    @Transactional
    public InspectSample create(SampleCreateRequest request) {
        BizAudits.requireValidTimezone(request.getBizTimezone());
        RestoreOrder order = orderMapper.selectById(request.getOrderId());
        if (order == null) {
            throw new IllegalArgumentException("修复工单不存在");
        }
        if (!OrderStatus.IN_PROGRESS.name().equals(order.getStatus())
                && !OrderStatus.COMPLETED.name().equals(order.getStatus())) {
            throw new IllegalStateException("工单当前状态为"
                    + OrderStatus.descriptionOf(order.getStatus()) + "，不能登记检测样本");
        }
        ArchiveVolume volume = volumeMapper.selectById(order.getVolumeId());
        if (volume == null) {
            throw new IllegalStateException("工单关联的馆藏册不存在");
        }
        String sampleNo = request.getSampleNo().trim();
        Long count = sampleMapper.selectCount(Wrappers.<InspectSample>lambdaQuery()
                .eq(InspectSample::getSampleNo, sampleNo));
        if (count != null && count > 0) {
            throw new IllegalArgumentException("样本编号已存在");
        }
        InspectSample sample = new InspectSample();
        sample.setSampleNo(sampleNo);
        sample.setOrderId(order.getId());
        sample.setVolumeId(volume.getId());
        sample.setMoistureContent(request.getMoistureContent());
        sample.setPhValue(request.getPhValue());
        sample.setFiberStrength(request.getFiberStrength());
        sample.setInspectDate(request.getInspectDate());
        // 检测落库时保留纸张状态快照
        sample.setPaperSnapshot(Snapshots.paper(request.getMoistureContent(),
                request.getPhValue(), request.getFiberStrength()));
        sample.setRequestNo(BizAudits.newRequestNo());
        sample.setOperator(request.getOperator().trim());
        sample.setBizTimezone(request.getBizTimezone().trim());
        volume.setMoistureContent(request.getMoistureContent());
        volume.setPhValue(request.getPhValue());
        volume.setFiberStrength(request.getFiberStrength());
        sample.setVersionSnapshot(RestoreSnapshots.volumeVersion(volume));
        try {
            sampleMapper.insert(sample);
        } catch (DuplicateKeyException ex) {
            throw new IllegalArgumentException("样本编号已存在");
        }
        // 同一事务同步馆藏册当前纸张状态
        volumeMapper.update(null, Wrappers.<ArchiveVolume>lambdaUpdate()
                .set(ArchiveVolume::getMoistureContent, request.getMoistureContent())
                .set(ArchiveVolume::getPhValue, request.getPhValue())
                .set(ArchiveVolume::getFiberStrength, request.getFiberStrength())
                .set(ArchiveVolume::getUpdateTime, LocalDateTime.now())
                .eq(ArchiveVolume::getId, volume.getId()));
        return sample;
    }

    @Override
    public List<InspectSample> list(Long orderId, Long volumeId) {
        return sampleMapper.selectList(Wrappers.<InspectSample>lambdaQuery()
                .eq(orderId != null, InspectSample::getOrderId, orderId)
                .eq(volumeId != null, InspectSample::getVolumeId, volumeId)
                .orderByAsc(InspectSample::getId));
    }

    @Override
    public InspectSample getById(Long id) {
        InspectSample sample = sampleMapper.selectById(id);
        if (sample == null) {
            throw new IllegalArgumentException("检测样本不存在");
        }
        return sample;
    }
}
