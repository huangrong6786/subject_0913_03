package com.evops.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;

import java.time.LocalDate;

/**
 * 修复批次。
 */
@TableName("t_restore_batch")
public class RestoreBatch extends BaseEntity {
    private String batchNo;
    private String batchName;
    private String status;
    private LocalDate bizDate;
    private String requestNo;
    private String operator;
    private String bizTimezone;
    private String versionSnapshot;

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getBizDate() { return bizDate; }
    public void setBizDate(LocalDate bizDate) { this.bizDate = bizDate; }
    public String getRequestNo() { return requestNo; }
    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getBizTimezone() { return bizTimezone; }
    public void setBizTimezone(String bizTimezone) { this.bizTimezone = bizTimezone; }
    public String getVersionSnapshot() { return versionSnapshot; }
    public void setVersionSnapshot(String versionSnapshot) { this.versionSnapshot = versionSnapshot; }
}
