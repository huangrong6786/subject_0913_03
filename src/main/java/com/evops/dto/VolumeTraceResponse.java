package com.evops.dto;

import com.evops.entity.AcceptRecord;
import com.evops.entity.ArchiveVolume;
import com.evops.entity.InspectSample;
import com.evops.entity.RestoreOrder;

import java.util.List;

/**
 * 馆藏册全链路追踪：馆藏册 + 工单 + 检测样本 + 验收记录。
 */
public class VolumeTraceResponse {
    private ArchiveVolume volume;
    private List<OrderTrace> orders;

    public VolumeTraceResponse(ArchiveVolume volume, List<OrderTrace> orders) {
        this.volume = volume;
        this.orders = orders;
    }

    public ArchiveVolume getVolume() { return volume; }
    public List<OrderTrace> getOrders() { return orders; }

    public static class OrderTrace {
        private RestoreOrder order;
        private List<InspectSample> samples;
        private List<AcceptRecord> accepts;

        public OrderTrace(RestoreOrder order, List<InspectSample> samples, List<AcceptRecord> accepts) {
            this.order = order;
            this.samples = samples;
            this.accepts = accepts;
        }

        public RestoreOrder getOrder() { return order; }
        public List<InspectSample> getSamples() { return samples; }
        public List<AcceptRecord> getAccepts() { return accepts; }
    }
}
