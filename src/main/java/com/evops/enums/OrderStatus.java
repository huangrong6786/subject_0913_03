package com.evops.enums;

/**
 * 修复工单状态：CREATED 已创建、IN_PROGRESS 修复中、COMPLETED 已完工、ACCEPTED 已验收。
 */
public enum OrderStatus {
    CREATED("已创建"),
    IN_PROGRESS("修复中"),
    COMPLETED("已完工"),
    ACCEPTED("已验收");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static boolean isValid(String value) {
        for (OrderStatus status : values()) {
            if (status.name().equals(value)) {
                return true;
            }
        }
        return false;
    }

    public static String descriptionOf(String value) {
        for (OrderStatus status : values()) {
            if (status.name().equals(value)) {
                return status.description;
            }
        }
        return value;
    }
}
