package com.sixgroup.refit.observability.item35.creator.application.usecase;

import com.sixgroup.refit.observability.item.state.application.StateService;
import com.sixgroup.refit.observability.item.state.domain.model.StateRequest;
import com.sixgroup.refit.observability.item35.creator.application.service.CapacityCpuService;
import com.sixgroup.refit.observability.item35.creator.application.service.CapacityRamService;
import com.sixgroup.refit.observability.item35.creator.application.service.LogService;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.Capacity;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.CREATING_AND_SAVING_FILE;
import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.ITEM35;

@Service
@RequiredArgsConstructor
@Slf4j
public class UseCaseComputeCapacity implements ItemTypeStrategy {

    private final CapacityCpuService capacityCpuService;
    private final CapacityRamService capacityRamService;
    private final WriteFileItem35Service<Capacity> writeFileComputeCapacity;
    private final ProducerItemService producerItemService;
    private final StateService stateService;

    @Override
    public File execute(final ItemCommandDTO itemCommandDTO, final Headers headers) throws ExecutionException, InterruptedException {

        log.debug("Generating compute capacity file ...");
        File file = null;

        try {
            final List<Capacity> capacityCpu = capacityCpuService.findByCapacityCpu(itemCommandDTO.getItemDate());
            final List<Capacity> capacityRam = capacityRamService.findByCapacityRam(itemCommandDTO.getItemDate());

            if (CollectionUtils.isEmpty(capacityCpu) || CollectionUtils.isEmpty(capacityRam)) {
                log.error("No data found in compute capacity, skipping report generation");
                stateService.setError(
                    StateRequest.builder()
                        .fileName(FileUtils.getFileName(itemCommandDTO))
                        .itemType(ITEM35)
                        .errorDescription("No record status found, skipping file generation")
                        .build());
                return null;
            }

            stateService.nextStep(
                StateRequest.builder().fileName(FileUtils.getFileName(itemCommandDTO)).itemType(ITEM35).build());
            LogService.logInfo(CREATING_AND_SAVING_FILE, itemCommandDTO);

            final List<Capacity> recordsCapacity = new ArrayList<>();
            recordsCapacity.addAll(capacityCpu);
            recordsCapacity.addAll(capacityRam);
            recordsCapacity.sort(Comparator.comparing(Capacity::getDate));

            file = writeFileComputeCapacity.writeFile(recordsCapacity, itemCommandDTO);
            log.debug("Generated compute capacity file with name {}, path {}", file.getName(), file.getAbsolutePath());
            itemCommandDTO.setFileName(file.getName());
            itemCommandDTO.setFileUrl(file.getAbsolutePath());
            producerItemService.send(itemCommandDTO, headers);
        } catch (Exception e) {
            log.error("Error to generate file compute capacity {}", e.getMessage(), e);
            stateService.setError(
                StateRequest.builder()
                    .fileName(FileUtils.getFileName(itemCommandDTO))
                    .itemType(ITEM35)
                    .errorDescription("Error to generate file compute capacity: " + e.getMessage())
                    .build());
        }
        return file;
    }

    @Override
    public ItemType getItemType() {
        return ItemType.COMPUTE_CAPACITY;
    }
}
