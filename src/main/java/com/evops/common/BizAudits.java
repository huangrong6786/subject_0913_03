package com.evops.common;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 业务审计工具：生成请求号、校验业务时区。
 */
public final class BizAudits {
    private static final DateTimeFormatter REQ_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private BizAudits() {
    }

    /**
     * 生成一次业务写入的请求号，随记录落库，便于跨表追踪同一笔操作。
     */
    public static String newRequestNo() {
        return "REQ-" + LocalDateTime.now().format(REQ_TIME) + "-"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    /**
     * 校验业务时区（如 Asia/Shanghai），非法时直接拒绝写入。
     */
    public static void requireValidTimezone(String bizTimezone) {
        try {
            ZoneId.of(bizTimezone);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("无效的业务时区: " + bizTimezone);
        }
    }
}
