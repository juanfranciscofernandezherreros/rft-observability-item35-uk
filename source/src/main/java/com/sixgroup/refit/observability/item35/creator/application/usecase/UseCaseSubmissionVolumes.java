package com.sixgroup.refit.observability.item35.creator.application.usecase;

import com.sixgroup.refit.observability.item35.creator.application.service.LogService;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.model.RecordStatus;
import com.sixgroup.refit.observability.item35.creator.domain.service.ProducerItemService;
import com.sixgroup.refit.observability.item35.creator.domain.service.RecordStatusService;
import com.sixgroup.refit.observability.item35.creator.domain.service.WriteFileItem35Service;
import com.sixgroup.refit.observability.item35.creator.domain.strategy.ItemTypeStrategy;
import com.sixgroup.refit.observability.item35.creator.shared.utils.Utils;

import com.sixgroup.refit.observability.modules.log.rft.application.RftLog;
import com.sixgroup.refit.observability.modules.log.rft.domain.logobject.base.NameObject;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.common.header.Headers;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

import com.sixgroup.refit.observability.item.state.application.StateService;
import com.sixgroup.refit.observability.item.state.domain.model.StateRequest;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.*;

@Service
@RequiredArgsConstructor
public class UseCaseSubmissionVolumes implements ItemTypeStrategy {

    private final RecordStatusService recordStatusService;

    private final WriteFileItem35Service<RecordStatus> writeFileSubmissionVolumes;

    private final ProducerItemService producerItemService;

    private final StateService stateService;

    @Override
    public File execute(ItemCommandDTO itemCommandDTO, Headers headers) {

        RftLog.info("Generating submission volumes file ...");
        File file = null;
        List<RecordStatus> recordStatusList = recordStatusService.findRecordStatus(itemCommandDTO.getItemDate());
        if (CollectionUtils.isEmpty(recordStatusList)) {
            RftLog.info("No record status found, skipping file generation");
            stateService.setError(
                StateRequest.builder().fileName(Utils.getFileName(itemCommandDTO)).itemType(ITEM35).build());
            return null;
        }
        try {
            stateService.nextStep(
                StateRequest.builder().fileName(Utils.getFileName(itemCommandDTO)).itemType(ITEM35).build());
            LogService.logInfo(CREATING_AND_SAVING_FILE, itemCommandDTO);
            file = writeFileSubmissionVolumes.writeFile(recordStatusList, itemCommandDTO);
            RftLog.info("Generated submission volumes file");
            itemCommandDTO.setFileUrl(file.getAbsolutePath());
            itemCommandDTO.setFileName(file.getName());
            producerItemService.send(itemCommandDTO, headers);
        } catch (Exception e) {
            RftLog.error("Error to generate file submission volumes",
                List.of(NameObject.builder().name("Error").object(e.getMessage()).build()), "");
            stateService.setError(
                StateRequest.builder().fileName(Utils.getFileName(itemCommandDTO)).itemType(ITEM35).build());
        }
        return file;
    }

    @Override
    public ItemType getItemType() {
        return ItemType.SUBMISSION_VOLUMES;
    }

}
