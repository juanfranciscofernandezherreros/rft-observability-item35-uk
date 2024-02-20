package observability.item35.creator.application.usecase;

import com.sixgroup.refit.observability.ApplicationMain;
import com.sixgroup.refit.observability.item35.creator.application.usecase.UseCaseGenerateFileSubmissionVolumes;
import com.sixgroup.refit.observability.topic.item.FileInfo;
import com.sixgroup.refit.observability.topic.item.ItemCommand;
import com.sixgroup.refit.observability.topic.item.ItemId;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Properties;


@SpringBootTest(classes={ApplicationMain.class})
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
public class UseCaseGenerateFileSubmissionVolumesITTest {

    @Autowired
    private UseCaseGenerateFileSubmissionVolumes useCaseGenerateFileSubmissionVolumes;

    private Producer<ItemId, ItemCommand> producer;

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String TOPIC = "rft-observability-item-topic.public.v1";


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
     void test() throws InterruptedException {
        producer.send(new ProducerRecord<>(TOPIC,ItemId.newBuilder().setItemId("item35").build(),
                ItemCommand
                        .newBuilder()
                        .setItemId("item35")
                        .setItemType("submissionVolumes")
                        .setCommand("request")
                        .setCreationTimestamp(Instant.now())
                        .setItemDate(Instant.now().toString())
                        .setFileInfo(FileInfo.newBuilder()
                                .setFileName("")
                                .setFileUrl("").build())
                        .build()));
        Thread.sleep(10000);
    }

}
