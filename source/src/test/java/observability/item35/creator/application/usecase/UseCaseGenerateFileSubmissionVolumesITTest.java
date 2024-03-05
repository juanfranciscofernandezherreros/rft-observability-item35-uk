package observability.item35.creator.application.usecase;

import com.sixgroup.refit.observability.ApplicationMain;
import com.sixgroup.refit.observability.item.state.domain.repository.ItemFileFinderRepository;
import com.sixgroup.refit.observability.item35.creator.application.usecase.UseCaseSubmissionVolumes;
import com.sixgroup.refit.observability.item35.creator.domain.enums.Command;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.shared.Constants;
import com.sixgroup.refit.observability.item35.creator.shared.Utils;
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
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.waitAtMost;


@SpringBootTest(classes = {ApplicationMain.class})
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
public class UseCaseGenerateFileSubmissionVolumesITTest {

    @Autowired
    private UseCaseSubmissionVolumes useCaseSubmissionVolumes;

    private Producer<ItemId, ItemCommand> producer;

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    @Value("${component-config.topics.observability-item-topic}")
    private String topic;

    @Autowired
    private ItemFileFinderRepository sqlServerItemFileFinderRepository;

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
    @DisplayName("Given a message item from topic, validate create and save file")
    void when_send_item_request_item_35_create_and_save_file_submission_volumes() throws InterruptedException {
        producer.send(new ProducerRecord<>(topic, ItemId.newBuilder().setItemId(Constants.ITEM35).build(),
            ItemCommand
                .newBuilder()
                .setItemId(Constants.ITEM35)
                .setItemType(ItemType.SUBMISSION_VOLUMES.getName())
                .setCommand(Command.REQUEST.getDescription())
                .setCreationTimestamp(Instant.now())
                .setItemDate("20240229")
                .setFileInfo(FileInfo.newBuilder()
                    .setFileName("")
                    .setFileUrl("").build())
                .build()));

        waitAtMost(20, TimeUnit.SECONDS)
            .until(() -> sqlServerItemFileFinderRepository.findByItemTypeAndFileName(Constants.ITEM35,
                Utils.getFileName("20240229")).getStateName().equals("sent_response")
                &&
                sqlServerItemFileFinderRepository.findByItemTypeAndFileName(Constants.ITEM35,
                    Utils.getFileName("20240229")).getFileUrl().equals("work-repository-observability/upload/TRRGS_EMIR_PR_IN_ND_ITEM35A_20240229.csv")
            );

    }

    @Test
    @DisplayName("Given a message item from topic, validate state is error")
    void when_send_item_request_item_35_then_state_error_not_exist_records() throws InterruptedException {
        producer.send(new ProducerRecord<>(topic, ItemId.newBuilder().setItemId(Constants.ITEM35).build(),
            ItemCommand
                .newBuilder()
                .setItemId(Constants.ITEM35)
                .setItemType(ItemType.SUBMISSION_VOLUMES.getName())
                .setCommand(Command.REQUEST.getDescription())
                .setCreationTimestamp(Instant.now())
                .setItemDate("20240129")
                .setFileInfo(FileInfo.newBuilder()
                    .setFileName("")
                    .setFileUrl("").build())
                .build()));

        waitAtMost(15, TimeUnit.SECONDS)
            .until(() -> sqlServerItemFileFinderRepository.
                findByItemTypeAndFileName(Constants.ITEM35, Utils.getFileName("20240129")).getStateName().equals("error")
            );

    }

}
