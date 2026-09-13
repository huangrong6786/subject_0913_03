package com.evops.service;

import com.evops.dto.VolumeCreateRequest;
import com.evops.dto.VolumeTraceResponse;
import com.evops.entity.ArchiveVolume;

import java.util.List;

public interface ArchiveVolumeService {
    ArchiveVolume register(VolumeCreateRequest request);

    List<ArchiveVolume> list(String keyword, String status);

    ArchiveVolume getById(Long id);

    VolumeTraceResponse trace(Long id);

    void delete(Long id);
}
