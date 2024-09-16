package com.sixgroup.refit.observability.item35.creator.infrastructure.producer;

import com.sixgroup.refit.observability.item35.creator.domain.enums.Command;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.service.ProducerItemService;
import com.sixgroup.refit.observability.modules.log.kafka.infrastructure.producer.RftKafkaProducerTracing;
import com.sixgroup.refit.observability.topic.item.FileInfo;
import com.sixgroup.refit.observability.topic.item.ItemCommand;
import com.sixgroup.refit.observability.topic.item.ItemId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static com.sixgroup.refit.observability.item35.creator.shared.ErrorCatalog.ERROR_SENDING_MESSAGE_EFRH_031;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducerItem implements ProducerItemService {

    private final RftKafkaProducerTracing<ItemId, ItemCommand> producerTracing;

    @Value("${component-config.topics.observability-item-topic}")
    private String topic;

    @Override
    public void send(ItemCommandDTO itemCommandDTO, Headers headers) {
        final ItemId itemId = ItemId.newBuilder().setItemId(itemCommandDTO.getItemId()).build();
        final ItemCommand itemCommand = getItemCommandResponse(itemCommandDTO);
        var future = producerTracing.createMessage(topic, itemId, itemCommand, headers);
        addCallBack(future, itemId, itemCommand);
    }

    void addCallBack(
        CompletableFuture<SendResult<ItemId, ItemCommand>> completableFuture,
        ItemId key,
        ItemCommand value) {
        completableFuture.whenComplete(
            (kvSendResult, ex) -> {
                if (null != ex) {
                    log.error("Couldn't produce to output topic with exception message: {}, and code: {}",
                        ex.getMessage(), ERROR_SENDING_MESSAGE_EFRH_031);
                } else {
                    log.debug("Successfully produced to output topic an event with key and result key {} value {} ", key, value);
                }
            });
    }

    private ItemCommand getItemCommandResponse(ItemCommandDTO itemCommandDTO) {
        return ItemCommand.newBuilder()
            .setItemId(itemCommandDTO.getItemId())
            .setItemType(itemCommandDTO.getItemType())
            .setCommand(Command.RESPONSE.getDescription())
            .setCreationTimestamp(Instant.now())
            .setItemDate(itemCommandDTO.getItemDate())
            .setFileInfo(FileInfo.newBuilder()
                .setFileName(itemCommandDTO.getFileName()).setFileUrl(itemCommandDTO.getFileUrl()).build()).build();
    }

}
