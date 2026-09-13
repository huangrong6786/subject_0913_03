package com.evops.enums;

/**
 * 验收结论：PASSED 验收通过、REJECTED 验收不通过。
 */
public enum AcceptResult {
    PASSED("验收通过"),
    REJECTED("验收不通过");

    private final String description;

    AcceptResult(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static boolean isValid(String value) {
        for (AcceptResult result : values()) {
            if (result.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
