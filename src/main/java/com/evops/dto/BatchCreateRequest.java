package com.evops.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * 修复批次建立请求。
 */
public class BatchCreateRequest extends BizAuditRequest {
    @NotBlank(message = "批次号不能为空")
    private String batchNo;

    private String batchName;

    @NotNull(message = "批次建立日期不能为空")
    private LocalDate bizDate;

    @NotEmpty(message = "批次馆藏册不能为空")
    private List<Long> volumeIds;

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }
    public LocalDate getBizDate() { return bizDate; }
    public void setBizDate(LocalDate bizDate) { this.bizDate = bizDate; }
    public List<Long> getVolumeIds() { return volumeIds; }
    public void setVolumeIds(List<Long> volumeIds) { this.volumeIds = volumeIds; }
}
