package com.evops.service;

import com.evops.dto.BatchCreateRequest;
import com.evops.dto.BatchDetailResponse;
import com.evops.entity.RestoreBatch;

import java.util.List;

public interface RestoreBatchService {
    RestoreBatch create(BatchCreateRequest request);

    List<RestoreBatch> list();

    BatchDetailResponse getDetail(Long id);

    void delete(Long id);
}
