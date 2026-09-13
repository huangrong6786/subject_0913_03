package com.evops.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 馆藏册。业务键：馆藏册号 + 修复版本。
 */
@TableName("t_archive_volume")
public class ArchiveVolume extends BaseEntity {
    private String volumeNo;
    private String restoreVersion;
    private String title;
    private String paperType;
    private String status;
    private BigDecimal moistureContent;
    private BigDecimal phValue;
    private BigDecimal fiberStrength;
    private LocalDate receivedDate;
    private String requestNo;
    private String operator;
    private String bizTimezone;
    private String versionSnapshot;

    public String getVolumeNo() { return volumeNo; }
    public void setVolumeNo(String volumeNo) { this.volumeNo = volumeNo; }
    public String getRestoreVersion() { return restoreVersion; }
    public void setRestoreVersion(String restoreVersion) { this.restoreVersion = restoreVersion; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getPaperType() { return paperType; }
    public void setPaperType(String paperType) { this.paperType = paperType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getMoistureContent() { return moistureContent; }
    public void setMoistureContent(BigDecimal moistureContent) { this.moistureContent = moistureContent; }
    public BigDecimal getPhValue() { return phValue; }
    public void setPhValue(BigDecimal phValue) { this.phValue = phValue; }
    public BigDecimal getFiberStrength() { return fiberStrength; }
    public void setFiberStrength(BigDecimal fiberStrength) { this.fiberStrength = fiberStrength; }
    public LocalDate getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }
    public String getRequestNo() { return requestNo; }
    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getBizTimezone() { return bizTimezone; }
    public void setBizTimezone(String bizTimezone) { this.bizTimezone = bizTimezone; }
    public String getVersionSnapshot() { return versionSnapshot; }
    public void setVersionSnapshot(String versionSnapshot) { this.versionSnapshot = versionSnapshot; }
}
