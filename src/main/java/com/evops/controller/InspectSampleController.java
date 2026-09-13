package com.evops.controller;

import com.evops.common.ApiResponse;
import com.evops.dto.SampleCreateRequest;
import com.evops.entity.InspectSample;
import com.evops.service.InspectSampleService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/restore/samples")
@Validated
public class InspectSampleController {
    private final InspectSampleService sampleService;

    public InspectSampleController(InspectSampleService sampleService) {
        this.sampleService = sampleService;
    }

    @PostMapping
    public ApiResponse<InspectSample> create(@Valid @RequestBody SampleCreateRequest request) {
        return ApiResponse.ok(sampleService.create(request));
    }

    @GetMapping
    public ApiResponse<List<InspectSample>> list(@RequestParam(required = false) Long orderId,
                                                 @RequestParam(required = false) Long volumeId) {
        return ApiResponse.ok(sampleService.list(orderId, volumeId));
    }

    @GetMapping("/{id}")
    public ApiResponse<InspectSample> detail(@PathVariable Long id) {
        return ApiResponse.ok(sampleService.getById(id));
    }
}
