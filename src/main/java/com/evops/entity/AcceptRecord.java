package com.evops.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;

import java.time.LocalDate;

/**
 * 验收记录。落库时保留纸张状态快照；已签发报告的记录不可直接删除。
 */
@TableName("t_accept_record")
public class AcceptRecord extends BaseEntity {
    private String acceptNo;
    private Long orderId;
    private Long volumeId;
    private String result;
    private LocalDate acceptDate;
    private Boolean reportIssued;
    private String paperSnapshot;
    private String requestNo;
    private String operator;
    private String bizTimezone;
    private String versionSnapshot;

    public String getAcceptNo() { return acceptNo; }
    public void setAcceptNo(String acceptNo) { this.acceptNo = acceptNo; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getVolumeId() { return volumeId; }
    public void setVolumeId(Long volumeId) { this.volumeId = volumeId; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public LocalDate getAcceptDate() { return acceptDate; }
    public void setAcceptDate(LocalDate acceptDate) { this.acceptDate = acceptDate; }
    public Boolean getReportIssued() { return reportIssued; }
    public void setReportIssued(Boolean reportIssued) { this.reportIssued = reportIssued; }
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
