package com.evops.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 修复工单（工序载体）。建单与完工时保留纸张状态快照。
 */
@TableName("t_restore_order")
public class RestoreOrder extends BaseEntity {
    private String orderNo;
    private Long batchId;
    private Long volumeId;
    private String processName;
    private String status;
    private BigDecimal workHours;
    private LocalDate startDate;
    private LocalDate finishDate;
    private String paperSnapshot;
    private String requestNo;
    private String operator;
    private String bizTimezone;
    private String versionSnapshot;

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public Long getVolumeId() { return volumeId; }
    public void setVolumeId(Long volumeId) { this.volumeId = volumeId; }
    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getWorkHours() { return workHours; }
    public void setWorkHours(BigDecimal workHours) { this.workHours = workHours; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getFinishDate() { return finishDate; }
    public void setFinishDate(LocalDate finishDate) { this.finishDate = finishDate; }
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
