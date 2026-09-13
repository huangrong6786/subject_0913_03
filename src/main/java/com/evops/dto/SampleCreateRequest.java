package com.evops.dto;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 检测样本登记请求。
 */
public class SampleCreateRequest extends BizAuditRequest {
    @NotBlank(message = "样本编号不能为空")
    private String sampleNo;

    @NotNull(message = "修复工单不能为空")
    private Long orderId;

    @NotNull(message = "检测日期不能为空")
    private LocalDate inspectDate;

    @NotNull(message = "含水率不能为空")
    @DecimalMin(value = "0", message = "含水率不能小于0")
    @DecimalMax(value = "30", message = "含水率不能超过30")
    private BigDecimal moistureContent;

    @NotNull(message = "酸碱度不能为空")
    @DecimalMin(value = "0", message = "酸碱度不能小于0")
    @DecimalMax(value = "14", message = "酸碱度不能超过14")
    private BigDecimal phValue;

    @NotNull(message = "纤维强度不能为空")
    @DecimalMin(value = "0", message = "纤维强度不能小于0")
    private BigDecimal fiberStrength;

    public String getSampleNo() { return sampleNo; }
    public void setSampleNo(String sampleNo) { this.sampleNo = sampleNo; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public LocalDate getInspectDate() { return inspectDate; }
    public void setInspectDate(LocalDate inspectDate) { this.inspectDate = inspectDate; }
    public BigDecimal getMoistureContent() { return moistureContent; }
    public void setMoistureContent(BigDecimal moistureContent) { this.moistureContent = moistureContent; }
    public BigDecimal getPhValue() { return phValue; }
    public void setPhValue(BigDecimal phValue) { this.phValue = phValue; }
    public BigDecimal getFiberStrength() { return fiberStrength; }
    public void setFiberStrength(BigDecimal fiberStrength) { this.fiberStrength = fiberStrength; }
}
