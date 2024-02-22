package com.sixgroup.refit.observability.item35.creator.infrastructure.consumer;

import com.sixgroup.refit.observability.item35.creator.application.usecase.UseCaseGenerateFileSubmissionVolumes;
import com.sixgroup.refit.observability.item35.creator.domain.enums.Command;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.shared.Constants;
import com.sixgroup.refit.observability.modules.log.kafka.infrastructure.consumer.RftKafkaConsumerTracing;
import com.sixgroup.refit.observability.modules.log.rft.application.RftLog;
import com.sixgroup.refit.observability.modules.log.rft.domain.logobject.base.NameObject;
import com.sixgroup.refit.observability.topic.item.ItemCommand;
import com.sixgroup.refit.observability.topic.item.ItemId;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KafkaConsumerItem {

    private final UseCaseGenerateFileSubmissionVolumes useCaseGenerateFileSubmissionVolumes;
    private final RftKafkaConsumerTracing kafkaConsumerTracing;

    @KafkaListener(topics = "${component-config.topics.file-ready-topic}")
    public void consume(ConsumerRecord<ItemId, ItemCommand> item) {
        if (Constants.ITEM35.equals(item.key().getItemId())
                && Command.REQUEST.getDescription().equals(item.value().getCommand())) {
            // Init traceId
            kafkaConsumerTracing.initTrace(item.headers());
            RftLog.info("Consumed message item", () ->
                    List.of(NameObject.builder().name("timestamp").object(item.value().getCreationTimestamp()).build(),
                            NameObject.builder().name("itemId").object(item.key()).build(),
                            NameObject.builder().name("itemType").object(item.value().getItemType()).build(),
                            NameObject.builder().name("command").object(item.value().getCommand()).build()
                    ));
            if (isRequestSubmissionVolumes(item.value())) {
                RftLog.debug("Consumed message to generate file Submission Volumes");
                useCaseGenerateFileSubmissionVolumes.manageFileSubmissionVolumes();
                RftLog.debug("Generate file item35: Submission Volumes");
            }
        }
    }

    private boolean isRequestSubmissionVolumes(ItemCommand itemCommand) {
        return ItemType.SUBMISSION_VOLUMES.getDescription().equals(itemCommand.getItemType());
    }
}
