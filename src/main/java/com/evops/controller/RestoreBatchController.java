package com.evops.controller;

import com.evops.common.ApiResponse;
import com.evops.dto.BatchCreateRequest;
import com.evops.dto.BatchDetailResponse;
import com.evops.entity.RestoreBatch;
import com.evops.service.RestoreBatchService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/restore/batches")
@Validated
public class RestoreBatchController {
    private final RestoreBatchService batchService;

    public RestoreBatchController(RestoreBatchService batchService) {
        this.batchService = batchService;
    }

    @PostMapping
    public ApiResponse<RestoreBatch> create(@Valid @RequestBody BatchCreateRequest request) {
        return ApiResponse.ok(batchService.create(request));
    }

    @GetMapping
    public ApiResponse<List<RestoreBatch>> list() {
        return ApiResponse.ok(batchService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<BatchDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(batchService.getDetail(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        batchService.delete(id);
        return ApiResponse.ok("删除成功");
    }
}
