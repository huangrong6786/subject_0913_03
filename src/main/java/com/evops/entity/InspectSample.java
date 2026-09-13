package com.evops.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 检测样本。落库时保留纸张状态快照（含水率、酸碱度、纤维强度）。
 */
@TableName("t_inspect_sample")
public class InspectSample extends BaseEntity {
    private String sampleNo;
    private Long orderId;
    private Long volumeId;
    private BigDecimal moistureContent;
    private BigDecimal phValue;
    private BigDecimal fiberStrength;
    private LocalDate inspectDate;
    private String paperSnapshot;
    private String requestNo;
    private String operator;
    private String bizTimezone;
    private String versionSnapshot;

    public String getSampleNo() { return sampleNo; }
    public void setSampleNo(String sampleNo) { this.sampleNo = sampleNo; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getVolumeId() { return volumeId; }
    public void setVolumeId(Long volumeId) { this.volumeId = volumeId; }
    public BigDecimal getMoistureContent() { return moistureContent; }
    public void setMoistureContent(BigDecimal moistureContent) { this.moistureContent = moistureContent; }
    public BigDecimal getPhValue() { return phValue; }
    public void setPhValue(BigDecimal phValue) { this.phValue = phValue; }
    public BigDecimal getFiberStrength() { return fiberStrength; }
    public void setFiberStrength(BigDecimal fiberStrength) { this.fiberStrength = fiberStrength; }
    public LocalDate getInspectDate() { return inspectDate; }
    public void setInspectDate(LocalDate inspectDate) { this.inspectDate = inspectDate; }
    public String getPaperSnapshot() { return paperSnapshot; }
    public void setPaperSnapshot(String paperSnapshot) { this.paperSnapshot = paperSnapshot; }
    public String getRequestNo() { return requestNo; }
    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getBizTimezone() { return bizTimezone; }
    public void setBizTimezone(String bizTimezone) { this.bizTimezone = bizTimezone; }
    public String getVersionSnapshot() { return versionSnapshot; }
    public void setVersionSnapshot(String versionSnapshot) { this.versionSnapshot = versionSnapshot; }
}
