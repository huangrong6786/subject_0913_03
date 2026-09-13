package com.evops.dto;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 馆藏册登记请求。
 */
public class VolumeCreateRequest extends BizAuditRequest {
    @NotBlank(message = "馆藏册号不能为空")
    @Pattern(regexp = "^[A-Za-z0-9-]{2,32}$", message = "馆藏册号仅支持2-32位字母、数字或短横线")
    private String volumeNo;

    @NotBlank(message = "修复版本不能为空")
    @Pattern(regexp = "^[A-Za-z0-9.-]{1,16}$", message = "修复版本仅支持1-16位字母、数字、点或短横线")
    private String restoreVersion;

    @NotBlank(message = "题名不能为空")
    private String title;

    private String paperType;

    @NotNull(message = "送修日期不能为空")
    private LocalDate receivedDate;

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

    public String getVolumeNo() { return volumeNo; }
    public void setVolumeNo(String volumeNo) { this.volumeNo = volumeNo; }
    public String getRestoreVersion() { return restoreVersion; }
    public void setRestoreVersion(String restoreVersion) { this.restoreVersion = restoreVersion; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getPaperType() { return paperType; }
    public void setPaperType(String paperType) { this.paperType = paperType; }
    public LocalDate getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }
    public BigDecimal getMoistureContent() { return moistureContent; }
    public void setMoistureContent(BigDecimal moistureContent) { this.moistureContent = moistureContent; }
    public BigDecimal getPhValue() { return phValue; }
    public void setPhValue(BigDecimal phValue) { this.phValue = phValue; }
    public BigDecimal getFiberStrength() { return fiberStrength; }
    public void setFiberStrength(BigDecimal fiberStrength) { this.fiberStrength = fiberStrength; }
}
