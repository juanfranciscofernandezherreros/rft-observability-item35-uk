package com.sixgroup.refit.observability.modules.log.kafka.infrastructure.consumer;

import org.apache.kafka.common.header.Headers;
import org.springframework.stereotype.Component;

@Component
public class RftKafkaConsumerTracing {
    public void initTrace(Headers headers) {
        // Trace headers are already propagated by the Kafka record.
    }
}
