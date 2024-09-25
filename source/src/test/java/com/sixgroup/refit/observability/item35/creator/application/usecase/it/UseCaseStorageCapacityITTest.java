package com.sixgroup.refit.observability.item35.creator.application.usecase.it;


import com.sixgroup.refit.observability.ApplicationMain;
import com.sixgroup.refit.observability.item.state.domain.enums.State;
import com.sixgroup.refit.observability.item.state.domain.repository.ItemFileFinderRepository;
import com.sixgroup.refit.observability.item35.creator.application.service.StorageService;
import com.sixgroup.refit.observability.item35.creator.domain.enums.Command;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.shared.constants.Constants;
import com.sixgroup.refit.observability.item35.creator.shared.utils.FileUtils;
import com.sixgroup.refit.observability.topic.item.FileInfo;
import com.sixgroup.refit.observability.topic.item.ItemCommand;
import com.sixgroup.refit.observability.topic.item.ItemId;
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
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
class UseCaseStorageCapacityITTest {

    private Producer<ItemId, ItemCommand> producer;

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    @Value("${component-config.topics.observability-item-topic}")
    private String topic;

    @Autowired
    private ItemFileFinderRepository sqlServerItemFileFinderRepository;

    @SpyBean
    private StorageService storageService;

    @BeforeEach
    public void setUp() {
        // Configurar el productor
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, io.confluent.kafka.serializers.KafkaAvroSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, io.confluent.kafka.serializers.KafkaAvroSerializer.class.getName());
        producerProps.put("schema.registry.url", "mock://not-used");
        producer = new KafkaProducer<>(producerProps);

    }

    @Test
    @DisplayName("Given a message item35 with itemType StorageCapacity from topic, validate create and save file")
    void when_send_item_request_item_35_create_and_save_file_storage_capacity() throws IOException {
        producer.send(new ProducerRecord<>(topic, ItemId.newBuilder().setItemId(Constants.ITEM35).build(),
            ItemCommand
                .newBuilder()
                .setItemId(Constants.ITEM35)
                .setItemType(ItemType.STORAGE_CAPACITY.getName())
                .setCommand(Command.REQUEST.getDescription())
                .setCreationTimestamp(Instant.now())
                .setItemDate("20240315")
                .setFileInfo(FileInfo.newBuilder()
                    .setFileName("")
                    .setFileUrl("").build())
                .build()));

        waitAtMost(20, TimeUnit.SECONDS)
            .until(() -> sqlServerItemFileFinderRepository.findByItemTypeAndFileName(Constants.ITEM35,
                FileUtils.getFileName(ItemCommandDTO.builder()
                    .itemDate("20240315")
                    .itemType(ItemType.STORAGE_CAPACITY.getName()).build())).getStateName()
                .equals(State.SENT_RESPONSE.getName()));

        Path path = FileSystems.getDefault()
            .getPath("work-repository-observability/upload/item35/TRRGS_EMIR_PR_IN_ND_ITEM35C_20240315.csv");

        assertNotNull(path);

        String lineHeader = "TR_CODE;REPORTING_DATE;REGULATION_REFERENCE;DATA_CENTER_LOCATION;DATABASE_SERVER_OR_PLATFORM;" +
            "DATE;CAPACITY;USED_CAPACITY;AVAILABLE_CAPACITY;UTILIZATION;INCIDENT_RELATED;TR_INCIDENT_ID";

        String lineOne = "TRRGS;2024-03-15;EMIR;Cloudera;Cloudera data warehouse;2024-03-01;13.3012;1.1997;12.1015;0.0902;NO;";
        String lineTwo = "TRRGS;2024-03-15;EMIR;Cloudera;Cloudera data warehouse;2024-03-02;13.3012;1.1837;12.1175;0.089;NO;";
        String penultimateLine = "TRRGS;2024-03-15;EMIR;Cloudera;Cloudera data warehouse;2024-03-31;19.3932;1.5539;17.8393;0.0801;NO;";
        String lastLine = "TRRGS;2024-03-15;EMIR;Cloudera;Cloudera data warehouse;2024-04-01;19.3932;1.6337;17.7595;0.0842;NO;";

        List<String> allLines = Files.readAllLines(path);

        assertEquals(lineHeader, allLines.get(0));
        assertEquals(lineOne, allLines.get(1));
        assertEquals(lineTwo, allLines.get(2));
        assertEquals(penultimateLine, allLines.get(allLines.size() - 2));
        assertEquals(lastLine, allLines.get(allLines.size() -1 ));

        assertEquals(33, allLines.size());
    }

    @Test
    @DisplayName("Given a message item from topic, when service return empty list validate state is error with no exist record")
    void item_35_then_state_error_not_exist_records() {
        doReturn(List.of()).when(storageService).getTotalCapacity(any(), any());
        doReturn(List.of()).when(storageService).getTotalFreeCapacity(any(), any());
        producer.send(new ProducerRecord<>(topic, ItemId.newBuilder().setItemId(Constants.ITEM35).build(),
            ItemCommand
                .newBuilder()
                .setItemId(Constants.ITEM35)
                .setItemType(ItemType.STORAGE_CAPACITY.getName())
                .setCommand(Command.REQUEST.getDescription())
                .setCreationTimestamp(Instant.now())
                .setItemDate("20240115")
                .setFileInfo(FileInfo.newBuilder()
                    .setFileName("")
                    .setFileUrl("").build())
                .build()));

        waitAtMost(15, TimeUnit.SECONDS)
            .until(() -> sqlServerItemFileFinderRepository.
                findByItemTypeAndFileName(Constants.ITEM35, FileUtils.getFileName(ItemCommandDTO.builder()
                    .itemDate("20240115")
                    .itemType(ItemType.STORAGE_CAPACITY.getName())
                    .build())).getStateName().equals(State.ERROR.getName())
            );

    }

    @Test
    @DisplayName("Given a message item from topic, when error from service total capacity then validate state is error")
    void item_35_state_error_cpu() {

        doThrow(new RuntimeException("error")).when(storageService).getTotalCapacity(any(), any());

        producer.send(new ProducerRecord<>(topic, ItemId.newBuilder().setItemId(Constants.ITEM35).build(),
            ItemCommand
                .newBuilder()
                .setItemId(Constants.ITEM35)
                .setItemType(ItemType.STORAGE_CAPACITY.getName())
                .setCommand(Command.REQUEST.getDescription())
                .setCreationTimestamp(Instant.now())
                .setItemDate("20240215")
                .setFileInfo(FileInfo.newBuilder()
                    .setFileName("")
                    .setFileUrl("").build())
                .build()));

        waitAtMost(15, TimeUnit.SECONDS)
            .until(() -> sqlServerItemFileFinderRepository.
                findByItemTypeAndFileName(Constants.ITEM35, FileUtils.getFileName(ItemCommandDTO.builder()
                    .itemDate("20240215")
                    .itemType(ItemType.STORAGE_CAPACITY.getName())
                    .build())).getStateName().equals(State.ERROR.getName())
            );
    }

    @Test
    @DisplayName("Given a message item from topic, when error from service total free capacity then validate state is error")
    void item_35_state_error_ram() {

        doThrow(new RuntimeException("error")).when(storageService).getTotalFreeCapacity(any(), any());

        producer.send(new ProducerRecord<>(topic, ItemId.newBuilder().setItemId(Constants.ITEM35).build(),
            ItemCommand
                .newBuilder()
                .setItemId(Constants.ITEM35)
                .setItemType(ItemType.STORAGE_CAPACITY.getName())
                .setCommand(Command.REQUEST.getDescription())
                .setCreationTimestamp(Instant.now())
                .setItemDate("20240215")
                .setFileInfo(FileInfo.newBuilder()
                    .setFileName("")
                    .setFileUrl("").build())
                .build()));

        waitAtMost(15, TimeUnit.SECONDS)
            .until(() -> sqlServerItemFileFinderRepository.
                findByItemTypeAndFileName(Constants.ITEM35, FileUtils.getFileName(ItemCommandDTO.builder()
                    .itemDate("20240215")
                    .itemType(ItemType.STORAGE_CAPACITY.getName())
                    .build())).getStateName().equals(State.ERROR.getName())
            );
    }


}

