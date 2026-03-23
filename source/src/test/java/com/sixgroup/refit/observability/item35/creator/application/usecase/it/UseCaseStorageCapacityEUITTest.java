package com.sixgroup.refit.observability.item35.creator.application.usecase.it;

import com.sixgroup.refit.observability.ApplicationMain;
import com.sixgroup.refit.observability.item.state.domain.enums.State;
import com.sixgroup.refit.observability.item.state.domain.repository.ItemFileFinderRepository;
import com.sixgroup.refit.observability.item35.creator.application.service.StorageService;
import com.sixgroup.refit.observability.item35.creator.domain.enums.Command;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.shared.constants.AppConstants;
import com.sixgroup.refit.observability.topic.item.FileInfo;
import com.sixgroup.refit.observability.topic.item.ItemCommand;
import com.sixgroup.refit.observability.topic.item.ItemId;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.waitAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(classes = {ApplicationMain.class})
@ActiveProfiles({"test", "test-eu"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
class UseCaseStorageCapacityEUITTest {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private Producer<ItemId, ItemCommand> producer;

    @Value("${component-config.topics.observability-item-topic}")
    private String topic;

    @Autowired
    private ItemFileFinderRepository sqlServerItemFileFinderRepository;

    @SpyBean
    private StorageService storageService;

    @BeforeEach
    void setUp() {
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        producerProps.put("schema.registry.url", "mock://not-used");
        producer = new KafkaProducer<>(producerProps);
    }

    @Test
    @DisplayName("[EU] Given a message item35 with itemType StorageCapacity from topic, validate CSV is created with SLA_BREACH_ID column")
    void when_send_item_request_item_32_eu_create_and_save_file_storage_capacity() throws IOException {
        producer.send(new ProducerRecord<>(topic, ItemId.newBuilder().setItemId(AppConstants.ITEM35_ID).build(),
            ItemCommand
                .newBuilder()
                .setItemId(AppConstants.ITEM35_ID)
                .setItemType(ItemType.STORAGE_CAPACITY.getName())
                .setCommand(Command.REQUEST.getDescription())
                .setCreationTimestamp(Instant.now())
                .setItemDate("20240315")
                .setFileInfo(FileInfo.newBuilder()
                    .setFileName("")
                    .setFileUrl("").build())
                .build()));
        producer.flush();

        waitAtMost(60, TimeUnit.SECONDS)
            .until(() -> {
                var r = sqlServerItemFileFinderRepository.findByItemTypeAndFileName(AppConstants.ITEM35_ID,
                    "TRRGS_EMIR_PR_FU_ND_ITEM35C_20240315.csv");
                return r != null && State.SENT_RESPONSE.getName().equals(r.getStateName());
            });

        Path path = FileSystems.getDefault()
            .getPath("work-repository-observability/upload/item35/TRRGS_EMIR_PR_FU_ND_ITEM35C_20240315.csv");

        assertNotNull(path);

        String lineHeader = "TR_CODE;REPORTING_DATE;REGULATION_REFERENCE;DATA_CENTER_LOCATION;DATABASE_SERVER_OR_PLATFORM;" +
            "DATE;CAPACITY;USED_CAPACITY;AVAILABLE_CAPACITY;UTILIZATION;INCIDENT_RELATED;SLA_BREACH_ID";

        List<String> allLines = Files.readAllLines(path);

        assertEquals(lineHeader, allLines.get(0));
    }

    @Test
    @DisplayName("[EU] Given a message item35 from topic, when service return empty list validate state is error")
    void item_32_eu_then_state_error_not_exist_records() {
        doReturn(List.of()).when(storageService).getTotalCapacity(any(), any());
        doReturn(List.of()).when(storageService).getTotalFreeCapacity(any(), any());

        producer.send(new ProducerRecord<>(topic, ItemId.newBuilder().setItemId(AppConstants.ITEM35_ID).build(),
            ItemCommand
                .newBuilder()
                .setItemId(AppConstants.ITEM35_ID)
                .setItemType(ItemType.STORAGE_CAPACITY.getName())
                .setCommand(Command.REQUEST.getDescription())
                .setCreationTimestamp(Instant.now())
                .setItemDate("20240115")
                .setFileInfo(FileInfo.newBuilder()
                    .setFileName("")
                    .setFileUrl("").build())
                .build()));

        waitAtMost(15, TimeUnit.SECONDS)
            .until(() -> {
                var r = sqlServerItemFileFinderRepository
                    .findByItemTypeAndFileName(AppConstants.ITEM35_ID, "TRRGS_EMIR_PR_FU_ND_ITEM35C_20240115.csv");
                return r != null && State.ERROR.getName().equals(r.getStateName());
            });
    }

    @Test
    @DisplayName("[EU] Given a message item35 from topic, when error from total capacity service then validate state is error")
    void item_32_eu_state_error_total_capacity() {
        doThrow(new RuntimeException("error")).when(storageService).getTotalCapacity(any(), any());

        producer.send(new ProducerRecord<>(topic, ItemId.newBuilder().setItemId(AppConstants.ITEM35_ID).build(),
            ItemCommand
                .newBuilder()
                .setItemId(AppConstants.ITEM35_ID)
                .setItemType(ItemType.STORAGE_CAPACITY.getName())
                .setCommand(Command.REQUEST.getDescription())
                .setCreationTimestamp(Instant.now())
                .setItemDate("20240215")
                .setFileInfo(FileInfo.newBuilder()
                    .setFileName("")
                    .setFileUrl("").build())
                .build()));

        waitAtMost(15, TimeUnit.SECONDS)
            .until(() -> {
                var r = sqlServerItemFileFinderRepository
                    .findByItemTypeAndFileName(AppConstants.ITEM35_ID, "TRRGS_EMIR_PR_FU_ND_ITEM35C_20240215.csv");
                return r != null && State.ERROR.getName().equals(r.getStateName());
            });
    }

    @Test
    @DisplayName("[EU] Given a message item35 from topic, when error from free capacity service then validate state is error")
    void item_32_eu_state_error_free_capacity() {
        doThrow(new RuntimeException("error")).when(storageService).getTotalFreeCapacity(any(), any());

        producer.send(new ProducerRecord<>(topic, ItemId.newBuilder().setItemId(AppConstants.ITEM35_ID).build(),
            ItemCommand
                .newBuilder()
                .setItemId(AppConstants.ITEM35_ID)
                .setItemType(ItemType.STORAGE_CAPACITY.getName())
                .setCommand(Command.REQUEST.getDescription())
                .setCreationTimestamp(Instant.now())
                .setItemDate("20240215")
                .setFileInfo(FileInfo.newBuilder()
                    .setFileName("")
                    .setFileUrl("").build())
                .build()));

        waitAtMost(15, TimeUnit.SECONDS)
            .until(() -> {
                var r = sqlServerItemFileFinderRepository
                    .findByItemTypeAndFileName(AppConstants.ITEM35_ID, "TRRGS_EMIR_PR_FU_ND_ITEM35C_20240215.csv");
                return r != null && State.ERROR.getName().equals(r.getStateName());
            });
    }
}
