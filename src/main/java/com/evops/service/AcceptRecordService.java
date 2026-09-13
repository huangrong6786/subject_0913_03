package com.evops.service;

import com.evops.dto.AcceptCreateRequest;
import com.evops.entity.AcceptRecord;

import java.util.List;

public interface AcceptRecordService {
    AcceptRecord create(AcceptCreateRequest request);

    AcceptRecord issueReport(Long id);

    List<AcceptRecord> list(Long orderId, String result);

    AcceptRecord getById(Long id);

    void delete(Long id);
}
