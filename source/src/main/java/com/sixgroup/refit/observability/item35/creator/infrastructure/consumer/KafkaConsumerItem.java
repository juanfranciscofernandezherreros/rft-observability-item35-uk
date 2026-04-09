package com.sixgroup.refit.observability.item35.creator.infrastructure.consumer;

import com.sixgroup.refit.observability.item.log.ItemLog;
import com.sixgroup.refit.observability.item.state.domain.model.ItemReportingDto;
import com.sixgroup.refit.observability.item35.creator.domain.enums.Command;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.strategy.ItemTypeStrategy;
import com.sixgroup.refit.observability.item35.creator.shared.constants.AppConstants;
import com.sixgroup.refit.observability.modules.log.kafka.infrastructure.consumer.RftKafkaConsumerTracing;
import com.sixgroup.refit.observability.topic.item.ItemCommand;
import com.sixgroup.refit.observability.topic.item.ItemId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.Executor;

import static com.sixgroup.refit.observability.item.state.domain.enums.State.INTERNAL_REQUEST_RECEIVED;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumerItem {

    private final RftKafkaConsumerTracing kafkaConsumerTracing;
    private final Map<ItemType, ItemTypeStrategy> itemType;
    private final ItemLog iLog = new ItemLog();
    private final Executor executor;

    @KafkaListener(topics = "${component-config.topics.observability-item-topic}", groupId = "${component-config.topics.observability-item-consumer-group-id}")
    public void consume(final ConsumerRecord<ItemId, ItemCommand> item) {
        log.debug("Consume message: {}", item);
        if (isItemIdAccepted(item.key().getItemId())
            && Command.REQUEST.getDescription().equals(item.value().getCommand())) {
            kafkaConsumerTracing.initTrace(item.headers());
            final ItemCommandDTO itemCommand = ItemCommandDTO.generateItemCommandDTO(item.value());
            log.info("Item request itemCommand: {}", itemCommand);
            iLog.info(ItemReportingDto.builder().itemType(itemCommand.getItemType()).build(), INTERNAL_REQUEST_RECEIVED);

            if (!isRequestTypeAccepted(item.value().getItemType())) {
                throw new IllegalArgumentException("'type' " + item.value().getItemType()
                    + " cannot be null or blank, and must be any of accepted values");
            }
            final ItemTypeStrategy itemTypeStrategy = itemType.get(ItemType.getItemTypeFromName(item.value().getItemType()));
            executor.execute(() -> itemTypeStrategy.execute(itemCommand, item.headers()));
        }
    }

    private boolean isItemIdAccepted(final String itemId) {
//        return AppConstants.ITEM32_ID.equals(itemId) || AppConstants.ITEM35_ID.equals(itemId);
        return AppConstants.ITEM35_ID.equals(itemId);
    }

    private boolean isRequestTypeAccepted(final String requestItemType) {
        return ItemType.reportsItemName().contains(requestItemType);
    }
}
