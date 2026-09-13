package com.evops.dto;

import com.evops.entity.ArchiveVolume;
import com.evops.entity.RestoreBatch;

import java.util.List;

/**
 * 批次详情：批次 + 关联馆藏册。
 */
public class BatchDetailResponse {
    private RestoreBatch batch;
    private List<ArchiveVolume> volumes;

    public BatchDetailResponse(RestoreBatch batch, List<ArchiveVolume> volumes) {
        this.batch = batch;
        this.volumes = volumes;
    }

    public RestoreBatch getBatch() { return batch; }
    public List<ArchiveVolume> getVolumes() { return volumes; }
}
