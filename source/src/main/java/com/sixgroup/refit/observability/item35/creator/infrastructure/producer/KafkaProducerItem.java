package com.sixgroup.refit.observability.item35.creator.infrastructure.producer;

import com.sixgroup.refit.observability.item35.creator.domain.enums.Command;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemFileFinderRequest;
import com.sixgroup.refit.observability.item35.creator.domain.service.ProducerItemService;
import com.sixgroup.refit.observability.item35.creator.shared.Utils;
import com.sixgroup.refit.observability.item35.creator.state.application.StateService;
import com.sixgroup.refit.observability.item35.creator.state.domain.StateRequest;
import com.sixgroup.refit.observability.modules.log.kafka.infrastructure.producer.RftKafkaProducerTracing;
import com.sixgroup.refit.observability.modules.log.rft.application.RftLog;
import com.sixgroup.refit.observability.modules.log.rft.domain.logobject.base.AvroLog;
import com.sixgroup.refit.observability.modules.log.rft.domain.logobject.base.NameObject;
import com.sixgroup.refit.observability.topic.item.ItemCommand;
import com.sixgroup.refit.observability.topic.item.ItemId;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.sixgroup.refit.observability.item35.creator.shared.Constants.ITEM35;
import static com.sixgroup.refit.observability.item35.creator.shared.ErrorCatalog.ERROR_SENDING_MESSAGE_EFRH_031;

@Component
@RequiredArgsConstructor
public class KafkaProducerItem implements ProducerItemService {

    private final RftKafkaProducerTracing<ItemId, ItemCommand> producerTracing;
    private final Tracer tracer;

    @Value("${component-config.topics.observability-item-topic}")
    private String topic;

    private final StateService stateService;

    @Override
    public void send(ItemId itemId, ItemCommand itemCommand) {
        var future = producerTracing.createMessage(topic, itemId, itemCommand, tracer);

        stateService.nextStep(
            StateRequest.builder()
                .fileName(Utils.getFileName(itemCommand.getItemDate()))
                .itemType(ITEM35)
                .build());
        RftLog.info(
            "Producer Item",
            () ->
                List.of(
                    NameObject.builder()
                        .name("timestamp")
                        .object(LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE))
                        .build(),
                    NameObject.builder().name("itemId").object(ITEM35).build(),
                    NameObject.builder()
                        .name("itemType")
                        .object(ItemType.SUBMISSION_VOLUMES.getName())
                        .build(),
                    NameObject.builder().name("command").object(Command.RESPONSE).build(),
                    NameObject.builder()
                        .name("fileName")
                        .object(itemCommand.getFileInfo().getFileName())
                        .build(),
                    NameObject.builder()
                        .name("fileUrl")
                        .object(itemCommand.getFileInfo().getFileUrl())
                        .build()));

        addCallBack(future, itemId, itemCommand);
    }

    void addCallBack(
        CompletableFuture<SendResult<ItemId, ItemCommand>> completableFuture,
        ItemId key,
        ItemCommand value) {
        completableFuture.whenComplete(
            (kvSendResult, ex) -> {
                if (null != ex) {
                    RftLog.error(
                        "Couldn't produce to output topic  with exception:",
                        ex.getMessage(),
                        ERROR_SENDING_MESSAGE_EFRH_031);
                } else {
                    RftLog.debug(
                        "Successfully produced to output topic an event with key and result ",
                        () ->
                            List.of(
                                AvroLog.builder().name("key").object(key).build(),
                                AvroLog.builder().name("value").object(value).build()));
                }
            });
    }
}
