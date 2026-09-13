package com.evops.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 纸张状态与版本快照序列化工具。检测、工序、验收落库时必须保留快照。
 */
public final class Snapshots {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private Snapshots() {
    }

    public static String json(Map<String, ?> snapshot) {
        try {
            return MAPPER.writeValueAsString(snapshot);
        } catch (Exception ex) {
            throw new IllegalStateException("快照序列化失败");
        }
    }

    /**
     * 纸张状态快照：含水率、酸碱度、纤维强度。
     */
    public static String paper(BigDecimal moistureContent, BigDecimal phValue, BigDecimal fiberStrength) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("moistureContent", moistureContent);
        snapshot.put("phValue", phValue);
        snapshot.put("fiberStrength", fiberStrength);
        return json(snapshot);
    }
}
