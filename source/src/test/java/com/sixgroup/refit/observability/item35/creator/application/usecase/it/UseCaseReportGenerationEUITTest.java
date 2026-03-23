package com.sixgroup.refit.observability.item35.creator.application.usecase.it;

import com.sixgroup.refit.observability.ApplicationMain;
import com.sixgroup.refit.observability.item.state.domain.enums.State;
import com.sixgroup.refit.observability.item.state.domain.repository.ItemFileFinderRepository;
import com.sixgroup.refit.observability.item35.creator.application.service.ParticipantService;
import com.sixgroup.refit.observability.item35.creator.application.service.RegulatorService;
import com.sixgroup.refit.observability.item35.creator.application.service.TrService;
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
import static org.mockito.Mockito.doThrow;

@SpringBootTest(classes = {ApplicationMain.class})
@ActiveProfiles({"test", "test-eu"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
class UseCaseReportGenerationEUITTest {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private Producer<ItemId, ItemCommand> producer;

    @Value("${component-config.topics.observability-item-topic}")
    private String topic;

    @Autowired
    private ItemFileFinderRepository sqlServerItemFileFinderRepository;

    @SpyBean
    private ParticipantService participantService;

    @SpyBean
    private RegulatorService regulatorService;

    @SpyBean
    private TrService trService;

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
    @DisplayName("[EU] Given a message item35 with itemType ReportGeneration from topic, validate CSV is created with SLA_BREACH_ID column")
    void when_send_item_request_item_32_eu_create_and_save_file_report_generation() throws IOException {
        producer.send(new ProducerRecord<>(topic,
            ItemId.newBuilder().setItemId(AppConstants.ITEM35_ID).build(),
            ItemCommand.newBuilder()
                .setItemId(AppConstants.ITEM35_ID)
                .setItemType(ItemType.REPORT_GENERATION.getName())
                .setCommand(Command.REQUEST.getDescription())
                .setCreationTimestamp(Instant.now())
                .setItemDate("20240315")
                .setFileInfo(FileInfo.newBuilder()
                    .setFileName("")
                    .setFileUrl("").build())
                .build()));

        producer.flush();

        waitAtMost(20, TimeUnit.SECONDS)
            .until(() -> {
                var r = sqlServerItemFileFinderRepository.findByItemTypeAndFileName(AppConstants.ITEM32_ID,
                    "TRRGS_EMIR_PR_FU_ND_ITEM32B_20240315.csv");
                return r != null && State.SENT_RESPONSE.getName().equals(r.getStateName());
            });

        Path path = FileSystems.getDefault()
            .getPath("work-repository-observability/upload/item35/TRRGS_EMIR_PR_FU_ND_ITEM32B_20240315.csv");

        assertNotNull(path);

        String lineHeader = "TR_CODE;REPORTING_DATE;REGULATION_REFERENCE;REPORT_NAME;REPORT_TYPE;REPORT_GENERATION_TIME;" +
            "REPORT_COMPLETION_TIME;REPORT_PUBLICATION_TIME;DATE;SLA;DIFFERENCE;SLA_BREACH_ID";

        List<String> allLines = Files.readAllLines(path);

        assertEquals(lineHeader, allLines.get(0));
    }

    @Test
    @DisplayName("[EU] Given a message item35 from topic, when service return empty list validate state is error")
    void item_32_eu_then_state_error_not_exist_records() {
        producer.send(new ProducerRecord<>(topic, ItemId.newBuilder().setItemId(AppConstants.ITEM35_ID).build(),
            ItemCommand
                .newBuilder()
                .setItemId(AppConstants.ITEM35_ID)
                .setItemType(ItemType.REPORT_GENERATION.getName())
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
                    .findByItemTypeAndFileName(AppConstants.ITEM32_ID, "TRRGS_EMIR_PR_FU_ND_ITEM32B_20240115.csv");
                return r != null && State.ERROR.getName().equals(r.getStateName());
            });
    }

    @Test
    @DisplayName("[EU] Given a message item35 from topic, when error from service then validate state is error")
    void item_32_eu_state_error() {
        doThrow(new RuntimeException("error")).when(participantService).findParticipants(any(), any(), any());

        producer.send(new ProducerRecord<>(topic, ItemId.newBuilder().setItemId(AppConstants.ITEM35_ID).build(),
            ItemCommand
                .newBuilder()
                .setItemId(AppConstants.ITEM35_ID)
                .setItemType(ItemType.REPORT_GENERATION.getName())
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
                    .findByItemTypeAndFileName(AppConstants.ITEM32_ID, "TRRGS_EMIR_PR_FU_ND_ITEM32B_20240115.csv");
                return r != null && State.ERROR.getName().equals(r.getStateName());
            });
    }

    @Test
    @DisplayName("[EU] Given a message item35 from topic, when error from regulator service then validate state is error")
    void item_32_eu_state_error_regulator() {
        doThrow(new RuntimeException("error")).when(regulatorService).findRegulator(any(), any(), any());

        producer.send(new ProducerRecord<>(topic, ItemId.newBuilder().setItemId(AppConstants.ITEM35_ID).build(),
            ItemCommand
                .newBuilder()
                .setItemId(AppConstants.ITEM35_ID)
                .setItemType(ItemType.REPORT_GENERATION.getName())
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
                    .findByItemTypeAndFileName(AppConstants.ITEM32_ID, "TRRGS_EMIR_PR_FU_ND_ITEM32B_20240115.csv");
                return r != null && State.ERROR.getName().equals(r.getStateName());
            });
    }

    @Test
    @DisplayName("[EU] Given a message item35 from topic, when error from tr service then validate state is error")
    void item_32_eu_state_error_tr() {
        doThrow(new RuntimeException("error")).when(trService).findTr(any(), any(), any());

        producer.send(new ProducerRecord<>(topic, ItemId.newBuilder().setItemId(AppConstants.ITEM35_ID).build(),
            ItemCommand
                .newBuilder()
                .setItemId(AppConstants.ITEM35_ID)
                .setItemType(ItemType.REPORT_GENERATION.getName())
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
                    .findByItemTypeAndFileName(AppConstants.ITEM32_ID, "TRRGS_EMIR_PR_FU_ND_ITEM32B_20240115.csv");
                return r != null && State.ERROR.getName().equals(r.getStateName());
            });
    }
}

