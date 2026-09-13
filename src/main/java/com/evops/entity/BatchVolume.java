package com.evops.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;

/**
 * 批次-馆藏册关联。
 */
@TableName("t_batch_volume")
public class BatchVolume extends BaseEntity {
    private Long batchId;
    private Long volumeId;

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public Long getVolumeId() { return volumeId; }
    public void setVolumeId(Long volumeId) { this.volumeId = volumeId; }
}
