package com.sixgroup.refit.observability.modules.log.kafka.infrastructure.producer;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class RftKafkaProducerTracing<K, V> {
    private final KafkaTemplate<K, V> kafkaTemplate;

    public CompletableFuture<SendResult<K, V>> createMessage(String topic, K key, V value, Headers headers) {
        ProducerRecord<K, V> record = new ProducerRecord<>(topic, null, null, key, value, headers);
        return kafkaTemplate.send(record);
    }
}
