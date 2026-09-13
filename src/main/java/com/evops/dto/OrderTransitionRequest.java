package com.evops.dto;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * 工单状态流转请求。action 支持 START（开工）与 COMPLETE（完工）；
 * 完工必须登记工时与完工时纸张状态（含水率、酸碱度、纤维强度），用于工序快照。
 */
public class OrderTransitionRequest extends BizAuditRequest {
    @NotBlank(message = "流转动作不能为空")
    private String action;

    @DecimalMin(value = "0", message = "工时不能小于0")
    private BigDecimal workHours;

    private BigDecimal moistureContent;

    private BigDecimal phValue;

    private BigDecimal fiberStrength;

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public BigDecimal getWorkHours() { return workHours; }
    public void setWorkHours(BigDecimal workHours) { this.workHours = workHours; }
    public BigDecimal getMoistureContent() { return moistureContent; }
    public void setMoistureContent(BigDecimal moistureContent) { this.moistureContent = moistureContent; }
    public BigDecimal getPhValue() { return phValue; }
    public void setPhValue(BigDecimal phValue) { this.phValue = phValue; }
    public BigDecimal getFiberStrength() { return fiberStrength; }
    public void setFiberStrength(BigDecimal fiberStrength) { this.fiberStrength = fiberStrength; }
}
