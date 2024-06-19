package com.sixgroup.refit.observability.item35.creator.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class KafkaConsumerClient {

    private static final String SCHEMA_REGISTRY_URL_KEY = "schema.registry.url";

    private final Consumer<String, SpecificRecord> consumer;

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    public KafkaConsumerClient() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "KafkaConsumerIT");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put(SCHEMA_REGISTRY_URL_KEY, "mock://not-used");
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");
        this.consumer = new KafkaConsumer<>(props);
    }

    public long getNumberOfMessagesOnTopic(final String topic) {
        final List<TopicPartition> partitions = List.of(new TopicPartition(topic, 0));
        this.consumer.assign(partitions);
        this.consumer.seekToEnd(Collections.emptySet());
        final Map<TopicPartition, Long> endPartitions =
            partitions.stream().collect(Collectors.toMap(Function.identity(), this.consumer::position));
        return partitions.stream().mapToLong(endPartitions::get).sum();
    }

    public <T> T getLastMessage(final String topic, final Class<T> valueType) throws JsonProcessingException {
        final List<TopicPartition> partitions = List.of(new TopicPartition(topic, 0));
        final Map<TopicPartition, Long> endOffsets = this.consumer.endOffsets(partitions);
        final Map<TopicPartition, Long> lastPositions = new HashMap<>();
        lastPositions.put(partitions.get(0), endOffsets.get(partitions.get(0)));
        this.consumer.assign(lastPositions.keySet());
        this.consumer.seekToEnd(lastPositions.keySet());
        this.consumer.seek(partitions.get(0), lastPositions.get(partitions.get(0)) - 1);
        final ConsumerRecords<String, SpecificRecord> records = this.consumer.poll(Duration.ofMillis(100));
        SpecificRecord value = null;
        for (final ConsumerRecord<String, SpecificRecord> record : records) {
            value = record.value();
        }
        final ObjectMapper objectMapper = new ObjectMapper();
        return Objects.nonNull(value) ? objectMapper.readValue(objectMapper.readTree(value.toString()).get("payload").toString(), valueType)
            : null;
    }

}

