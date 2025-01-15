package com.sixgroup.refit.observability.item35.creator.application.usecase;

import com.sixgroup.refit.observability.item.log.ItemLog;
import com.sixgroup.refit.observability.item.state.application.StateService;
import com.sixgroup.refit.observability.item.state.domain.model.ItemReportingDto;
import com.sixgroup.refit.observability.item.state.domain.model.StateRequest;
import com.sixgroup.refit.observability.item35.creator.application.service.FileNameService;
import com.sixgroup.refit.observability.item35.creator.application.service.RecordStatusService;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.model.RecordStatus;
import com.sixgroup.refit.observability.item35.creator.domain.service.ProducerItemService;
import com.sixgroup.refit.observability.item35.creator.domain.service.WriteFileItem35Service;
import com.sixgroup.refit.observability.item35.creator.domain.strategy.ItemTypeStrategy;
import com.sixgroup.refit.observability.item35.creator.shared.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.common.header.Headers;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

import static com.sixgroup.refit.observability.item.state.domain.enums.State.*;
import static com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType.SUBMISSION_VOLUMES;
import static com.sixgroup.refit.observability.item35.creator.shared.constants.AppConstants.ITEM35_ID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UseCaseSubmissionVolumes implements ItemTypeStrategy {

    private final RecordStatusService recordStatusService;
    private final WriteFileItem35Service<RecordStatus> writeFileSubmissionVolumes;
    private final ProducerItemService producerItemService;
    private final FileNameService fileNameService;
    private final StateService stateService;
    private final ItemLog iLog = new ItemLog();

    @Override
    public File execute(ItemCommandDTO itemCommand, Headers headers) {

        log.debug("Generating submission volumes file ...");
        final String fileName = fileNameService.getFileName(SUBMISSION_VOLUMES, itemCommand.getItemDate());
        File file = null;
        try {
            stateService.nextStep(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).build());

            //Find information
            final String dateFrom = DateUtils.firstDayOfPreviousMonth(itemCommand.getItemDate());
            final String dateTo = DateUtils.lastDayOfPreviousMonth(itemCommand.getItemDate());

            final List<RecordStatus> recordStatusList = recordStatusService.findRecordStatus(dateFrom, dateTo);
            if (CollectionUtils.isEmpty(recordStatusList)) {
                iLog.info(ItemReportingDto.builder().itemType(ITEM35_ID).build(), ERROR);
                log.error("No data found in submission volumes, skipping report generation");
                stateService.setError(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).errorDescription("No record status found, skipping file generation").build());
                return null;
            }

            //Saving information
            stateService.nextStep(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).build());
            iLog.info(itemCommand, SAVING_INFORMATION);

            //Saved information
            file = writeFileSubmissionVolumes.writeFile(recordStatusList, itemCommand, fileName);
            stateService.nextStep(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).fileUrl(file.getPath()).build());
            iLog.info(itemCommand, SAVED_INFORMATION);

            //Sent response
            itemCommand.setFileName(file.getName());
            itemCommand.setFileUrl(file.getAbsolutePath());
            producerItemService.send(itemCommand, headers);
            stateService.nextStep(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).build());
            iLog.info(itemCommand, SENT_RESPONSE);
        } catch (Exception e) {
            iLog.info(ItemReportingDto.builder().itemType(ITEM35_ID).build(), ERROR);
            log.error("Error to generate file submission volumes: {}", e.getMessage(), e);
            stateService.setError(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).errorDescription("Error to generate file submission volumes: " + e.getMessage()).build());
        }
        return file;
    }

    @Override
    public ItemType getItemType() {
        return SUBMISSION_VOLUMES;
    }

}
