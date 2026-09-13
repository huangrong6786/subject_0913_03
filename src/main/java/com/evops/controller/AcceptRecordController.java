package com.evops.controller;

import com.evops.common.ApiResponse;
import com.evops.dto.AcceptCreateRequest;
import com.evops.entity.AcceptRecord;
import com.evops.service.AcceptRecordService;
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
@RequestMapping("/api/restore/accepts")
@Validated
public class AcceptRecordController {
    private final AcceptRecordService acceptRecordService;

    public AcceptRecordController(AcceptRecordService acceptRecordService) {
        this.acceptRecordService = acceptRecordService;
    }

    @PostMapping
    public ApiResponse<AcceptRecord> create(@Valid @RequestBody AcceptCreateRequest request) {
        return ApiResponse.ok(acceptRecordService.create(request));
    }

    @PutMapping("/{id}/issue-report")
    public ApiResponse<AcceptRecord> issueReport(@PathVariable Long id) {
        return ApiResponse.ok(acceptRecordService.issueReport(id));
    }

    @GetMapping
    public ApiResponse<List<AcceptRecord>> list(@RequestParam(required = false) Long orderId,
                                                @RequestParam(required = false) String result) {
        return ApiResponse.ok(acceptRecordService.list(orderId, result));
    }

    @GetMapping("/{id}")
    public ApiResponse<AcceptRecord> detail(@PathVariable Long id) {
        return ApiResponse.ok(acceptRecordService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        acceptRecordService.delete(id);
        return ApiResponse.ok("删除成功");
    }
}
