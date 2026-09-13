package com.evops.service;

import com.evops.dto.OrderCreateRequest;
import com.evops.dto.OrderTransitionRequest;
import com.evops.entity.RestoreOrder;

import java.util.List;

public interface RestoreOrderService {
    RestoreOrder create(OrderCreateRequest request);

    RestoreOrder transition(Long id, OrderTransitionRequest request);

    List<RestoreOrder> list(Long batchId, Long volumeId, String status);

    RestoreOrder getById(Long id);

    void delete(Long id);
}
