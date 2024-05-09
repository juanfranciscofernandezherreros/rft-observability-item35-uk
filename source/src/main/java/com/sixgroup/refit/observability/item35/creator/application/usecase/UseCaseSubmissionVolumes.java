package com.sixgroup.refit.observability.item35.creator.application.usecase;

import com.sixgroup.refit.observability.item.state.application.StateService;
import com.sixgroup.refit.observability.item.state.domain.model.StateRequest;
import com.sixgroup.refit.observability.item35.creator.application.service.LogService;
import com.sixgroup.refit.observability.item35.creator.application.service.RecordStatusService;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.model.RecordStatus;
import com.sixgroup.refit.observability.item35.creator.domain.service.ProducerItemService;
import com.sixgroup.refit.observability.item35.creator.domain.service.WriteFileItem35Service;
import com.sixgroup.refit.observability.item35.creator.domain.strategy.ItemTypeStrategy;
import com.sixgroup.refit.observability.item35.creator.shared.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.common.header.Headers;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.CREATING_AND_SAVING_FILE;
import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.ITEM35;

@Service
@RequiredArgsConstructor
@Slf4j
public class UseCaseSubmissionVolumes implements ItemTypeStrategy {

    private final RecordStatusService recordStatusService;
    private final WriteFileItem35Service<RecordStatus> writeFileSubmissionVolumes;
    private final ProducerItemService producerItemService;
    private final StateService stateService;

    @Override
    public File execute(ItemCommandDTO itemCommandDTO, Headers headers) {

        log.debug("Generating submission volumes file ...");
        File file = null;
        List<RecordStatus> recordStatusList = recordStatusService.findRecordStatus(itemCommandDTO.getItemDate());
        if (CollectionUtils.isEmpty(recordStatusList)) {
            log.debug("No record status found, skipping file generation");
            stateService.setError(
                StateRequest.builder()
                    .fileName(FileUtils.getFileName(itemCommandDTO))
                    .itemType(ITEM35)
                    .errorDescription("No record status found, skipping file generation")
                    .build());
            return null;
        }
        try {
            stateService.nextStep(
                StateRequest.builder().fileName(FileUtils.getFileName(itemCommandDTO)).itemType(ITEM35).build());
            LogService.logInfo(CREATING_AND_SAVING_FILE, itemCommandDTO);
            file = writeFileSubmissionVolumes.writeFile(recordStatusList, itemCommandDTO);
            log.debug("Generated submission volumes file");
            itemCommandDTO.setFileUrl(file.getAbsolutePath());
            itemCommandDTO.setFileName(file.getName());
            producerItemService.send(itemCommandDTO, headers);
        } catch (Exception e) {
            log.error("Error to generate file submission volumes: {}", e.getMessage(), e);
            stateService.setError(
                StateRequest.builder()
                    .fileName(FileUtils.getFileName(itemCommandDTO))
                    .itemType(ITEM35)
                    .errorDescription("Error to generate file submission volumes: " + e.getMessage())
                    .build());
        }
        return file;
    }

    @Override
    public ItemType getItemType() {
        return ItemType.SUBMISSION_VOLUMES;
    }

}
