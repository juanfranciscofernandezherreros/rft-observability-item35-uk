package com.sixgroup.refit.observability.item35.creator.infrastructure.consumer;

import com.sixgroup.refit.observability.item.state.application.StateService;
import com.sixgroup.refit.observability.item.state.domain.model.StateRequest;
import com.sixgroup.refit.observability.item35.creator.domain.enums.Command;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.strategy.ItemTypeStrategy;
import com.sixgroup.refit.observability.item35.creator.shared.constants.Constants;
import com.sixgroup.refit.observability.item35.creator.shared.utils.Utils;
import com.sixgroup.refit.observability.modules.log.kafka.infrastructure.consumer.RftKafkaConsumerTracing;
import com.sixgroup.refit.observability.topic.item.ItemCommand;
import com.sixgroup.refit.observability.topic.item.ItemId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.ITEM35;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumerItem {

    private final RftKafkaConsumerTracing kafkaConsumerTracing;
    private final StateService stateService;

    private final Map<ItemType, ItemTypeStrategy> itemType;

    @KafkaListener(topics = "${component-config.topics.observability-item-topic}", groupId = "${component-config.topics.observability-item-consumer-group-id}")
    public void consume(ConsumerRecord<ItemId, ItemCommand> item) throws ExecutionException, InterruptedException {
        log.debug("Consume message: {}", item);
        if (Constants.ITEM35.equals(item.key().getItemId())
            && Command.REQUEST.getDescription().equals(item.value().getCommand())) {

            kafkaConsumerTracing.initTrace(item.headers());
            ItemCommandDTO itemCommand = ItemCommandDTO.generateItemCommandDTO(item.value());
            if (isRequestTypeAccepted(item.value().getItemType())) {
                stateService.nextStep(
                    StateRequest.builder().fileName(Utils.getFileName(itemCommand))
                        .itemType(ITEM35).build());
                log.debug("Consumed message to generate file: {}", item.value().getItemType());
                ItemTypeStrategy itemTypeStrategy = itemType.get(ItemType.getItemTypeFromName(item.value().getItemType()));
                itemTypeStrategy.execute(itemCommand, item.headers());
                log.debug("Generated file item35: {}", item.value().getItemType());
            }
        }
    }

    private boolean isRequestTypeAccepted(String requestItemType) {

        Optional.ofNullable(requestItemType)
            .filter(type -> type.equals(ItemType.SUBMISSION_VOLUMES.getName()) || type.equals(ItemType.COMPUTE_CAPACITY.getName())
                || type.equals(ItemType.STORAGE_CAPACITY.getName()) || type.equals(ItemType.REPORT_GENERATION.getName()))
            .orElseThrow(() -> new IllegalArgumentException("'type' cannot be null or blank, " +
                "and must be any of accepted values"));

        return true;
    }
}
