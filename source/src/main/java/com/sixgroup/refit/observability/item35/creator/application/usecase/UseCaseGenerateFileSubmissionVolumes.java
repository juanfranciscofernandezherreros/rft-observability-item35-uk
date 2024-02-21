package com.sixgroup.refit.observability.item35.creator.application.usecase;

import com.sixgroup.refit.observability.item35.creator.configuration.CsvProperties;
import com.sixgroup.refit.observability.item35.creator.domain.enums.Command;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemReporting;
import com.sixgroup.refit.observability.item35.creator.domain.model.RecordStatus;
import com.sixgroup.refit.observability.item35.creator.domain.service.ItemReportingService;
import com.sixgroup.refit.observability.item35.creator.domain.service.ProducerItemService;
import com.sixgroup.refit.observability.item35.creator.domain.service.RecordStatusService;
import com.sixgroup.refit.observability.item35.creator.domain.service.WriteFileItem35Service;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.sixgroup.refit.observability.item35.creator.shared.Constants.DATE_FORMAT_yyyy_MM_dd;
import static com.sixgroup.refit.observability.item35.creator.shared.Constants.ITEM35;

@Service
@RequiredArgsConstructor
public class UseCaseGenerateFileSubmissionVolumes {

    private final RecordStatusService recordStatusService;

    private final WriteFileItem35Service writeFileSubmissionVolumesService;

    private final ItemReportingService itemReportingService;

    private final ProducerItemService producerItemService;

    private final CsvProperties csvProperties;

    public File manageFileSubmissionVolumes() {

        RftLog.info("Generating submission volumes file ...");

        File fileSubmissionVolumes = null;
        List<RecordStatus> recordStatusList = recordStatusService.findRecordStatus();
        if (CollectionUtils.isEmpty(recordStatusList)) {
            RftLog.info("No record status found, skipping file generation");
            itemReportingService.insertItemReporting(ItemReporting.builderItemReportingError());
            return null;
        }
        try {
            RftLog.info("Creating and saving file", () ->
                    List.of(NameObject.builder().name("timestamp").object(LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE)).build(),
                            NameObject.builder().name("itemId").object(ITEM35).build(),
                            NameObject.builder().name("itemType").object(ItemType.SUBMISSION_VOLUMES.getDescription()).build(),
                            NameObject.builder().name("command").object(Command.REQUEST).build(),
                            NameObject.builder().name("fileName").object(getFileName()).build(),
                            NameObject.builder().name("fileUrl").object(csvProperties.getOutputPath() + getFileName()).build()));
            fileSubmissionVolumes = writeFileSubmissionVolumesService.writeFile(recordStatusList, getFileName());
            itemReportingService.insertItemReporting(ItemReporting.builderItemReporting(fileSubmissionVolumes));
            RftLog.info("Generated submission volumes file");

            producerItemService.send(ItemId.newBuilder().setItemId(ITEM35).build(),
                    getItemCommandResponse(ItemType.SUBMISSION_VOLUMES, fileSubmissionVolumes));

        } catch (IOException io) {
            RftLog.error("Error to generate file submission volumes", "");
            itemReportingService.insertItemReporting(ItemReporting.builderItemReportingError());
        }
        return fileSubmissionVolumes;
    }

    private ItemCommand getItemCommandResponse(ItemType itemType, File file) {
        return ItemCommand
                .newBuilder()
                .setItemId(ITEM35)
                .setItemType(ItemType.SUBMISSION_VOLUMES.getDescription())
                .setCommand(Command.RESPONSE.getDescription())
                .setCreationTimestamp(Instant.now())
                .setItemDate(LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT_yyyy_MM_dd)))
                .setFileInfo(FileInfo.newBuilder()
                        .setFileName(file.getName())
                        .setFileUrl(file.getAbsolutePath()).build()).build();
    }

    private String getFileName() {
        return "TRRGS_EMIR_PR_FU_ND_ITEM35A_" + LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT_yyyy_MM_dd)) + ".csv";
    }

}
