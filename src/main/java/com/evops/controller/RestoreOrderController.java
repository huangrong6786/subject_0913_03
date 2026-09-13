package com.evops.controller;

import com.evops.common.ApiResponse;
import com.evops.dto.OrderCreateRequest;
import com.evops.dto.OrderTransitionRequest;
import com.evops.entity.RestoreOrder;
import com.evops.service.RestoreOrderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/restore/orders")
@Validated
public class RestoreOrderController {
    private final RestoreOrderService orderService;

    public RestoreOrderController(RestoreOrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResponse<RestoreOrder> create(@Valid @RequestBody OrderCreateRequest request) {
        return ApiResponse.ok(orderService.create(request));
    }

    @PutMapping("/{id}/transition")
    public ApiResponse<RestoreOrder> transition(@PathVariable Long id,
                                                @Valid @RequestBody OrderTransitionRequest request) {
        return ApiResponse.ok(orderService.transition(id, request));
    }

    @GetMapping
    public ApiResponse<List<RestoreOrder>> list(@RequestParam(required = false) Long batchId,
                                                @RequestParam(required = false) Long volumeId,
                                                @RequestParam(required = false) String status) {
        return ApiResponse.ok(orderService.list(batchId, volumeId, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<RestoreOrder> detail(@PathVariable Long id) {
        return ApiResponse.ok(orderService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ApiResponse.ok("删除成功");
    }
}
