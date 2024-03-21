package com.sixgroup.refit.observability.item35.creator.infrastructure.consumer;

import com.sixgroup.refit.observability.item.state.application.StateService;
import com.sixgroup.refit.observability.item.state.domain.model.StateRequest;
import com.sixgroup.refit.observability.item35.creator.application.service.LogService;
import com.sixgroup.refit.observability.item35.creator.application.usecase.UseCaseSubmissionVolumes;
import com.sixgroup.refit.observability.item35.creator.domain.enums.Command;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.strategy.ItemTypeStrategy;
import com.sixgroup.refit.observability.item35.creator.shared.constants.Constants;
import com.sixgroup.refit.observability.item35.creator.shared.utils.Utils;
import com.sixgroup.refit.observability.modules.log.kafka.infrastructure.consumer.RftKafkaConsumerTracing;
import com.sixgroup.refit.observability.modules.log.rft.application.RftLog;
import com.sixgroup.refit.observability.modules.log.rft.domain.logobject.base.NameObject;
import com.sixgroup.refit.observability.topic.item.ItemCommand;
import com.sixgroup.refit.observability.topic.item.ItemId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.CREATING_AND_SAVING_FILE;
import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.ITEM35;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumerItem {

    private final UseCaseSubmissionVolumes useCaseSubmissionVolumes;
    private final RftKafkaConsumerTracing kafkaConsumerTracing;
    private final StateService stateService;

    private final Map<ItemType, ItemTypeStrategy> itemType;

    @KafkaListener(topics = "${component-config.topics.observability-item-topic}", groupId = "${component-config.topics.observability-item-consumer-group-id}")
    public void consume(ConsumerRecord<ItemId, ItemCommand> item) throws ExecutionException, InterruptedException {
        RftLog.info("Consume message", () -> List.of(NameObject.builder().name("item").object(item).build()));
        if (Constants.ITEM35.equals(item.key().getItemId())
            && Command.REQUEST.getDescription().equals(item.value().getCommand())) {

            kafkaConsumerTracing.initTrace(item.headers());
            ItemCommandDTO itemCommand = ItemCommandDTO.generateItemCommandDTO(item.value());
            LogService.logInfo(CREATING_AND_SAVING_FILE, itemCommand);
            if (isRequestTypeAccepted(item.value())) {
                stateService.nextStep(
                    StateRequest.builder().fileName(Utils.getFileName(itemCommand))
                        .itemType(ITEM35).build());
                log.debug("Consumed message to generate file Submission Volumes");
                ItemTypeStrategy itemTypeStrategy = itemType.get(ItemType.getItemTypeFromName(item.value().getItemType()));
                itemTypeStrategy.execute(itemCommand, item.headers());
                log.debug("Generate file item35: Submission Volumes");
            }
        }
    }

    private boolean isRequestTypeAccepted(ItemCommand itemCommand) {
        return ItemType.SUBMISSION_VOLUMES.getName().equals(itemCommand.getItemType())
            || ItemType.COMPUTE_CAPACITY.getName().equals(itemCommand.getItemType())
            || ItemType.STORAGE_CAPACITY.getName().equals(itemCommand.getItemType());
    }
}
