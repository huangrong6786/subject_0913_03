package com.evops.service.support;

import com.evops.common.Snapshots;
import com.evops.entity.ArchiveVolume;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 修复域版本快照：随业务单据落库，记录写入时刻的业务状态。
 */
public final class RestoreSnapshots {
    private RestoreSnapshots() {
    }

    /**
     * 馆藏册版本快照：馆藏册号、修复版本、状态与纸张指标。
     */
    public static String volumeVersion(ArchiveVolume volume) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("volumeNo", volume.getVolumeNo());
        snapshot.put("restoreVersion", volume.getRestoreVersion());
        snapshot.put("status", volume.getStatus());
        snapshot.put("moistureContent", volume.getMoistureContent());
        snapshot.put("phValue", volume.getPhValue());
        snapshot.put("fiberStrength", volume.getFiberStrength());
        snapshot.put("receivedDate", volume.getReceivedDate());
        return Snapshots.json(snapshot);
    }

    /**
     * 批次版本快照：批次号、批次建立日期与关联馆藏册。
     */
    public static String batchVersion(String batchNo, LocalDate bizDate, List<Long> volumeIds) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("batchNo", batchNo);
        snapshot.put("bizDate", bizDate);
        snapshot.put("volumeIds", volumeIds);
        return Snapshots.json(snapshot);
    }
}
