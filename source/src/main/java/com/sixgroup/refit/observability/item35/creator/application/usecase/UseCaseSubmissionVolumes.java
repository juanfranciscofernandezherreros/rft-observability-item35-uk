package com.sixgroup.refit.observability.item35.creator.application.usecase;

import com.sixgroup.refit.observability.item35.creator.domain.enums.Command;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.RecordStatus;
import com.sixgroup.refit.observability.item35.creator.domain.service.ProducerItemService;
import com.sixgroup.refit.observability.item35.creator.domain.service.RecordStatusService;
import com.sixgroup.refit.observability.item35.creator.domain.service.WriteFileItem35Service;
import com.sixgroup.refit.observability.item35.creator.shared.Utils;

import com.sixgroup.refit.observability.modules.log.rft.application.RftLog;
import com.sixgroup.refit.observability.modules.log.rft.domain.logobject.base.NameObject;
import com.sixgroup.refit.observability.topic.item.FileInfo;
import com.sixgroup.refit.observability.topic.item.ItemCommand;
import com.sixgroup.refit.observability.topic.item.ItemId;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.sixgroup.refit.observability.item.state.application.StateService;
import com.sixgroup.refit.observability.item.state.domain.model.StateRequest;

import static com.sixgroup.refit.observability.item35.creator.shared.Constants.*;

@Service
@RequiredArgsConstructor
public class UseCaseSubmissionVolumes {

    private final RecordStatusService recordStatusService;

    private final WriteFileItem35Service writeFileSubmissionVolumesService;

    private final ProducerItemService producerItemService;

    private final StateService stateService;

    public File manageFileSubmissionVolumes(String itemDate) {
        RftLog.info("Generating submission volumes file ...");
        File file = null;
        List<RecordStatus> recordStatusList = recordStatusService.findRecordStatus(itemDate);
        if (CollectionUtils.isEmpty(recordStatusList)) {
            RftLog.info("No record status found, skipping file generation");
            stateService.setError(
                StateRequest.builder().fileName(Utils.getFileName(itemDate)).itemType(ITEM35).build());
            return null;
        }
        try {
            stateService.nextStep(
                StateRequest.builder().fileName(Utils.getFileName(itemDate)).itemType(ITEM35).build());
            RftLog.info("Creating and saving file", () ->
                List.of(NameObject.builder().name("timestamp").object(LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE)).build(),
                    NameObject.builder().name("itemId").object(ITEM35).build(),
                    NameObject.builder().name("itemType").object(ItemType.SUBMISSION_VOLUMES.getName()).build(),
                    NameObject.builder().name("command").object(Command.REQUEST).build()));
            file = writeFileSubmissionVolumesService.writeFile(recordStatusList, itemDate);
            RftLog.info("Generated submission volumes file");
            producerItemService.send(ItemId.newBuilder().setItemId(ITEM35).build(),
                getItemCommandResponse(file, itemDate));
        } catch (IOException io) {
            RftLog.error("Error to generate file submission volumes",
                List.of(NameObject.builder().name("Error").object(io.getMessage()).build()), "");
            stateService.setError(
                StateRequest.builder().fileName(Utils.getFileName(itemDate)).itemType(ITEM35).build());
        }
        return file;
    }

    private ItemCommand getItemCommandResponse(File file, String itemDate) {
        return ItemCommand.newBuilder()
            .setItemId(ITEM35)
            .setItemType(ItemType.SUBMISSION_VOLUMES.getName())
            .setCommand(Command.RESPONSE.getDescription())
            .setCreationTimestamp(Instant.now())
            .setItemDate(itemDate)
            .setFileInfo(FileInfo.newBuilder()
                .setFileName(file.getName()).setFileUrl(file.getAbsolutePath()).build()).build();
    }

}
