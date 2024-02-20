package com.sixgroup.refit.observability.item35.creator.infrastructure.producer;

import com.sixgroup.refit.observability.item35.creator.domain.enums.Command;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.service.ProducerItemService;
import com.sixgroup.refit.observability.modules.log.rft.application.RftLog;
import com.sixgroup.refit.observability.modules.log.rft.domain.logobject.base.AvroLog;
import com.sixgroup.refit.observability.modules.log.rft.domain.logobject.base.NameObject;
import com.sixgroup.refit.observability.topic.item.ItemCommand;
import com.sixgroup.refit.observability.topic.item.ItemId;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.sixgroup.refit.observability.item35.creator.shared.Constants.ITEM35;

@Component
@RequiredArgsConstructor
public class ProducerItemServiceImpl  implements ProducerItemService {

    private final KafkaTemplate<ItemId, ItemCommand> template;

    public static final String ERROR_SENDING_MESSAGE_EFRH_031 = "EFRH031";

    @Override
    public void send(ItemId itemId,ItemCommand itemCommand) {
        var future = template.send("rft-observability-item-topic.public.v1", itemId, itemCommand);

        RftLog.info("Save file",
                List.of(NameObject.builder().name("Timestamp").object(LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE)).build(),
                        NameObject.builder().name("itemId").object(ITEM35).build(),
                        NameObject.builder().name("itemType").object(ItemType.SUBMISSION_VOLUMES.getDescription()).build(),
                        NameObject.builder().name("command").object(Command.RESPONSE).build(),
                        NameObject.builder().name("fileName").object(itemCommand.getFileInfo().getFileName()).build(),
                        NameObject.builder().name("fileUrl").object(itemCommand.getFileInfo().getFileUrl()).build()));

        addCallBack(future, itemId, itemCommand);
    }

    void addCallBack(CompletableFuture<SendResult<ItemId, ItemCommand>> completableFuture, ItemId key, ItemCommand value) {
        completableFuture.whenComplete((kvSendResult, ex) -> {
            if (null!=ex) {
                RftLog.error("Couldn't produce to output topic  with exception:",
                        ex.getMessage(), ERROR_SENDING_MESSAGE_EFRH_031);
            } else {
                RftLog.debug("Successfully produced to output topic an event with key and result ", List.of(AvroLog.builder().name("key").object(key).build(),
                        AvroLog.builder().name("value").object(value).build()));
            }
        });
    }

}
