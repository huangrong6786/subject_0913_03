package com.evops.service;

import com.evops.dto.SampleCreateRequest;
import com.evops.entity.InspectSample;

import java.util.List;

public interface InspectSampleService {
    InspectSample create(SampleCreateRequest request);

    List<InspectSample> list(Long orderId, Long volumeId);

    InspectSample getById(Long id);
}
