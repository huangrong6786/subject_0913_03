package com.evops.controller;

import com.evops.common.ApiResponse;
import com.evops.dto.VolumeCreateRequest;
import com.evops.dto.VolumeTraceResponse;
import com.evops.entity.ArchiveVolume;
import com.evops.service.ArchiveVolumeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/restore/volumes")
@Validated
public class ArchiveVolumeController {
    private final ArchiveVolumeService volumeService;

    public ArchiveVolumeController(ArchiveVolumeService volumeService) {
        this.volumeService = volumeService;
    }

    @PostMapping
    public ApiResponse<ArchiveVolume> register(@Valid @RequestBody VolumeCreateRequest request) {
        return ApiResponse.ok(volumeService.register(request));
    }

    @GetMapping
    public ApiResponse<List<ArchiveVolume>> list(@RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) String status) {
        return ApiResponse.ok(volumeService.list(keyword, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<ArchiveVolume> detail(@PathVariable Long id) {
        return ApiResponse.ok(volumeService.getById(id));
    }

    @GetMapping("/{id}/trace")
    public ApiResponse<VolumeTraceResponse> trace(@PathVariable Long id) {
        return ApiResponse.ok(volumeService.trace(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        volumeService.delete(id);
        return ApiResponse.ok("删除成功");
    }
}
