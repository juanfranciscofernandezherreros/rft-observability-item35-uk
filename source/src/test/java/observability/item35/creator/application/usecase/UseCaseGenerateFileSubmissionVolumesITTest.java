package observability.item35.creator.application.usecase;

import com.sixgroup.refit.observability.ApplicationMain;
import com.sixgroup.refit.observability.item35.creator.application.usecase.UseCaseGenerateFileSubmissionVolumes;
import com.sixgroup.refit.observability.item35.creator.domain.enums.Command;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.enums.StatusFile;
import com.sixgroup.refit.observability.item35.creator.domain.repository.ItemReportingRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.sqlserver.ItemReportingEntity;
import com.sixgroup.refit.observability.item35.creator.infrastructure.repository.sqlserver.ItemReportingRepositorySqlServer;
import com.sixgroup.refit.observability.item35.creator.shared.Constants;
import com.sixgroup.refit.observability.topic.item.FileInfo;
import com.sixgroup.refit.observability.topic.item.ItemCommand;
import com.sixgroup.refit.observability.topic.item.ItemId;
import observability.item35.creator.config.KafkaConsumerClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import static org.awaitility.Awaitility.waitAtMost;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest(classes={ApplicationMain.class})
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
public class UseCaseGenerateFileSubmissionVolumesITTest {

    @Autowired
    private UseCaseGenerateFileSubmissionVolumes useCaseGenerateFileSubmissionVolumes;

    private Producer<ItemId, ItemCommand> producer;

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String TOPIC = "rft-observability-item-topic.public.v1";


    @Autowired
    private ItemReportingRepositorySqlServer itemReportingRepositorySqlServer;

    private KafkaConsumerClient kafkaConsumerClient=new KafkaConsumerClient();

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
        producer.send(new ProducerRecord<>(TOPIC,ItemId.newBuilder().setItemId(Constants.ITEM35).build(),
                ItemCommand
                        .newBuilder()
                        .setItemId(Constants.ITEM35)
                        .setItemType(ItemType.SUBMISSION_VOLUMES.getDescription())
                        .setCommand(Command.REQUEST.getDescription())
                        .setCreationTimestamp(Instant.now())
                        .setItemDate(Instant.now().toString())
                        .setFileInfo(FileInfo.newBuilder()
                                .setFileName("")
                                .setFileUrl("").build())
                        .build()));

      waitAtMost(5, TimeUnit.SECONDS)
                .until(()->itemReportingRepositorySqlServer.findAll().size()==1);
       List<ItemReportingEntity> reportingEntity= itemReportingRepositorySqlServer.findAll();
       ItemReportingEntity itemReportingDb=reportingEntity.get(0);
       assertNotNull(itemReportingDb);
       assertEquals(ItemType.SUBMISSION_VOLUMES.getDescription(),itemReportingDb.getItemType());
       assertEquals(StatusFile.ITEM_REPORTING_OK.getDescription(),itemReportingDb.getStateName());
        waitAtMost(5,TimeUnit.SECONDS)
                .until(()->kafkaConsumerClient.getNumberOfMessagesOnTopic(TOPIC)==2);

    }

}
