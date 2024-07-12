package com.sixgroup.refit.observability.item35.creator.application.usecase;

import com.sixgroup.refit.observability.item.log.ItemLog;
import com.sixgroup.refit.observability.item.state.application.StateService;
import com.sixgroup.refit.observability.item.state.domain.model.StateRequest;
import com.sixgroup.refit.observability.item35.creator.application.service.CapacityCpuService;
import com.sixgroup.refit.observability.item35.creator.application.service.CapacityRamService;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.Capacity;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.service.ProducerItemService;
import com.sixgroup.refit.observability.item35.creator.domain.service.WriteFileItem35Service;
import com.sixgroup.refit.observability.item35.creator.domain.strategy.ItemTypeStrategy;
import com.sixgroup.refit.observability.item35.creator.shared.utils.DateUtils;
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

import static com.sixgroup.refit.observability.item.state.domain.enums.State.SAVING_INFORMATION;
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
    private final ItemLog iLog = new ItemLog();

    @Override
    public File execute(final ItemCommandDTO itemCommand, final Headers headers) throws ExecutionException, InterruptedException {

        log.debug("Generating compute capacity file ...");
        File file = null;

        try {
            stateService.nextStep(StateRequest.builder().fileName(FileUtils.getFileName(itemCommand)).itemType(ITEM35).build());

            final String dateFrom = DateUtils.firstDayOfPreviousMonth(itemCommand.getItemDate());
            final String dateTo = DateUtils.firstDayOfCurrentMonth(itemCommand.getItemDate());

            final List<Capacity> capacityCpu = capacityCpuService.findByCapacityCpu(dateFrom, dateTo);
            final List<Capacity> capacityRam = capacityRamService.findByCapacityRam(dateFrom, dateTo);

            if (CollectionUtils.isEmpty(capacityCpu) || CollectionUtils.isEmpty(capacityRam)) {
                log.error("No data found in compute capacity, skipping report generation");
                stateService.setError(
                    StateRequest.builder()
                        .fileName(FileUtils.getFileName(itemCommand))
                        .itemType(ITEM35)
                        .errorDescription("No record status found, skipping file generation")
                        .build());
                return null;
            }

            stateService.nextStep(
                StateRequest.builder().fileName(FileUtils.getFileName(itemCommand)).itemType(ITEM35).build());
            iLog.info(itemCommand, SAVING_INFORMATION);

            final List<Capacity> recordsCapacity = new ArrayList<>();
            recordsCapacity.addAll(capacityCpu);
            recordsCapacity.addAll(capacityRam);
            recordsCapacity.sort(Comparator.comparing(Capacity::getDate));

            file = writeFileComputeCapacity.writeFile(recordsCapacity, itemCommand);
            log.debug("Generated compute capacity file with name {}, path {}", file.getName(), file.getAbsolutePath());
            itemCommand.setFileName(file.getName());
            itemCommand.setFileUrl(file.getAbsolutePath());
            producerItemService.send(itemCommand, headers);
        } catch (Exception e) {
            log.error("Error to generate file compute capacity {}", e.getMessage(), e);
            stateService.setError(
                StateRequest.builder()
                    .fileName(FileUtils.getFileName(itemCommand))
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
