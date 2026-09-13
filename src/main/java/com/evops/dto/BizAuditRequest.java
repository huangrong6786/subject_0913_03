package com.evops.dto;

import javax.validation.constraints.NotBlank;

/**
 * 业务审计基类：所有写请求必须携带操作者与业务时区，随记录落库。
 */
public abstract class BizAuditRequest {
    @NotBlank(message = "操作者不能为空")
    private String operator;

    @NotBlank(message = "业务时区不能为空")
    private String bizTimezone;

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getBizTimezone() { return bizTimezone; }
    public void setBizTimezone(String bizTimezone) { this.bizTimezone = bizTimezone; }
}
