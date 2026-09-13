package com.evops.dto;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 验收登记请求。result 支持 PASSED / REJECTED；issueReport=true 时同事务签发报告并落账。
 */
public class AcceptCreateRequest extends BizAuditRequest {
    @NotBlank(message = "验收单号不能为空")
    private String acceptNo;

    @NotNull(message = "修复工单不能为空")
    private Long orderId;

    @NotNull(message = "验收日期不能为空")
    private LocalDate acceptDate;

    @NotBlank(message = "验收结论不能为空")
    private String result;

    private boolean issueReport;

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

    public String getAcceptNo() { return acceptNo; }
    public void setAcceptNo(String acceptNo) { this.acceptNo = acceptNo; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public LocalDate getAcceptDate() { return acceptDate; }
    public void setAcceptDate(LocalDate acceptDate) { this.acceptDate = acceptDate; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public boolean isIssueReport() { return issueReport; }
    public void setIssueReport(boolean issueReport) { this.issueReport = issueReport; }
    public BigDecimal getMoistureContent() { return moistureContent; }
    public void setMoistureContent(BigDecimal moistureContent) { this.moistureContent = moistureContent; }
    public BigDecimal getPhValue() { return phValue; }
    public void setPhValue(BigDecimal phValue) { this.phValue = phValue; }
    public BigDecimal getFiberStrength() { return fiberStrength; }
    public void setFiberStrength(BigDecimal fiberStrength) { this.fiberStrength = fiberStrength; }
}
