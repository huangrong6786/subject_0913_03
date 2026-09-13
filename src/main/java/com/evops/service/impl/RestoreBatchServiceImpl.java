package com.evops.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.evops.common.BizAudits;
import com.evops.dto.BatchCreateRequest;
import com.evops.dto.BatchDetailResponse;
import com.evops.entity.ArchiveVolume;
import com.evops.entity.BatchVolume;
import com.evops.entity.RestoreBatch;
import com.evops.enums.VolumeStatus;
import com.evops.mapper.ArchiveVolumeMapper;
import com.evops.mapper.BatchVolumeMapper;
import com.evops.mapper.RestoreBatchMapper;
import com.evops.service.RestoreBatchService;
import com.evops.service.support.RestoreSnapshots;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestoreBatchServiceImpl implements RestoreBatchService {
    private static final String STATUS_OPEN = "OPEN";

    private final RestoreBatchMapper batchMapper;
    private final ArchiveVolumeMapper volumeMapper;
    private final BatchVolumeMapper batchVolumeMapper;

    public RestoreBatchServiceImpl(RestoreBatchMapper batchMapper,
                                   ArchiveVolumeMapper volumeMapper,
                                   BatchVolumeMapper batchVolumeMapper) {
        this.batchMapper = batchMapper;
        this.volumeMapper = volumeMapper;
        this.batchVolumeMapper = batchVolumeMapper;
    }

    @Override
    @Transactional
    public RestoreBatch create(BatchCreateRequest request) {
        BizAudits.requireValidTimezone(request.getBizTimezone());
        String batchNo = request.getBatchNo().trim();
        Long count = batchMapper.selectCount(Wrappers.<RestoreBatch>lambdaQuery()
                .eq(RestoreBatch::getBatchNo, batchNo));
        if (count != null && count > 0) {
            throw new IllegalArgumentException("批次号已存在");
        }
        LinkedHashSet<Long> volumeIds = new LinkedHashSet<>(request.getVolumeIds());
        List<ArchiveVolume> volumes = new ArrayList<>();
        for (Long volumeId : volumeIds) {
            ArchiveVolume volume = volumeMapper.selectById(volumeId);
            if (volume == null) {
                throw new IllegalArgumentException("馆藏册不存在: " + volumeId);
            }
            volumes.add(volume);
        }
        RestoreBatch batch = new RestoreBatch();
        batch.setBatchNo(batchNo);
        batch.setBatchName(request.getBatchName());
        batch.setStatus(STATUS_OPEN);
        batch.setBizDate(request.getBizDate());
        batch.setRequestNo(BizAudits.newRequestNo());
        batch.setOperator(request.getOperator().trim());
        batch.setBizTimezone(request.getBizTimezone().trim());
        batch.setVersionSnapshot(RestoreSnapshots.batchVersion(batchNo, request.getBizDate(),
                new ArrayList<>(volumeIds)));
        try {
            batchMapper.insert(batch);
        } catch (DuplicateKeyException ex) {
            // 并发下依赖唯一约束兜底，保证批次号唯一
            throw new IllegalArgumentException("批次号已存在");
        }
        // 同一事务内：馆藏册状态流转 + 批次关联落库；条件更新防止并发重复入批
        LocalDateTime now = LocalDateTime.now();
        for (ArchiveVolume volume : volumes) {
            int updated = volumeMapper.update(null, Wrappers.<ArchiveVolume>lambdaUpdate()
                    .set(ArchiveVolume::getStatus, VolumeStatus.BATCHED.name())
                    .set(ArchiveVolume::getUpdateTime, now)
                    .eq(ArchiveVolume::getId, volume.getId())
                    .eq(ArchiveVolume::getStatus, VolumeStatus.REGISTERED.name()));
            if (updated == 0) {
                throw new IllegalStateException("馆藏册" + volume.getVolumeNo() + "当前状态为"
                        + VolumeStatus.descriptionOf(volume.getStatus()) + "，不能加入批次");
            }
            BatchVolume relation = new BatchVolume();
            relation.setBatchId(batch.getId());
            relation.setVolumeId(volume.getId());
            batchVolumeMapper.insert(relation);
        }
        return batch;
    }

    @Override
    public List<RestoreBatch> list() {
        return batchMapper.selectList(Wrappers.<RestoreBatch>lambdaQuery()
                .orderByAsc(RestoreBatch::getId));
    }

    @Override
    public BatchDetailResponse getDetail(Long id) {
        RestoreBatch batch = batchMapper.selectById(id);
        if (batch == null) {
            throw new IllegalArgumentException("修复批次不存在");
        }
        List<BatchVolume> relations = batchVolumeMapper.selectList(Wrappers.<BatchVolume>lambdaQuery()
                .eq(BatchVolume::getBatchId, id)
                .orderByAsc(BatchVolume::getId));
        List<ArchiveVolume> volumes;
        if (relations.isEmpty()) {
            volumes = Collections.emptyList();
        } else {
            List<Long> volumeIds = relations.stream()
                    .map(BatchVolume::getVolumeId)
                    .collect(Collectors.toList());
            volumes = volumeMapper.selectBatchIds(volumeIds);
        }
        return new BatchDetailResponse(batch, volumes);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        RestoreBatch batch = batchMapper.selectById(id);
        if (batch == null) {
            throw new IllegalArgumentException("修复批次不存在");
        }
        Long relationCount = batchVolumeMapper.selectCount(Wrappers.<BatchVolume>lambdaQuery()
                .eq(BatchVolume::getBatchId, id));
        if (relationCount != null && relationCount > 0) {
            throw new IllegalStateException("批次已关联馆藏册，不能直接删除");
        }
        batchMapper.deleteById(id);
    }
}
