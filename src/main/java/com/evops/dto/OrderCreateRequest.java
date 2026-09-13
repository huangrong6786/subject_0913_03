package com.evops.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 修复工单创建请求。
 */
public class OrderCreateRequest extends BizAuditRequest {
    @NotBlank(message = "工单号不能为空")
    @Pattern(regexp = "^[A-Za-z0-9-]{2,32}$", message = "工单号仅支持2-32位字母、数字或短横线")
    private String orderNo;

    @NotNull(message = "批次不能为空")
    private Long batchId;

    @NotNull(message = "馆藏册不能为空")
    private Long volumeId;

    @NotBlank(message = "工序名称不能为空")
    private String processName;

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public Long getVolumeId() { return volumeId; }
    public void setVolumeId(Long volumeId) { this.volumeId = volumeId; }
    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }
}
