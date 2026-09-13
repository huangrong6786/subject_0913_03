package com.evops.enums;

/**
 * 馆藏册状态：REGISTERED 已登记、BATCHED 已入批、RESTORING 修复中、ACCEPTED 已验收、POSTED 已落账。
 */
public enum VolumeStatus {
    REGISTERED("已登记"),
    BATCHED("已入批"),
    RESTORING("修复中"),
    ACCEPTED("已验收"),
    POSTED("已落账");

    private final String description;

    VolumeStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static boolean isValid(String value) {
        for (VolumeStatus status : values()) {
            if (status.name().equals(value)) {
                return true;
            }
        }
        return false;
    }

    public static String descriptionOf(String value) {
        for (VolumeStatus status : values()) {
            if (status.name().equals(value)) {
                return status.description;
            }
        }
        return value;
    }
}
