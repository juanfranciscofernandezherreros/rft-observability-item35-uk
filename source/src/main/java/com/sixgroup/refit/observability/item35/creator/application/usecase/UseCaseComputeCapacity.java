package com.sixgroup.refit.observability.item35.creator.application.usecase;

import com.sixgroup.refit.observability.item.log.ItemLog;
import com.sixgroup.refit.observability.item.state.application.StateService;
import com.sixgroup.refit.observability.item.state.domain.model.ItemReportingDto;
import com.sixgroup.refit.observability.item.state.domain.model.StateRequest;
import com.sixgroup.refit.observability.item35.creator.application.service.CapacityCpuService;
import com.sixgroup.refit.observability.item35.creator.application.service.CapacityRamService;
import com.sixgroup.refit.observability.item35.creator.application.service.FileNameService;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.Capacity;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.service.ProducerItemService;
import com.sixgroup.refit.observability.item35.creator.domain.service.WriteFileItem35Service;
import com.sixgroup.refit.observability.item35.creator.domain.strategy.ItemTypeStrategy;
import com.sixgroup.refit.observability.item35.creator.shared.utils.DateUtils;
import com.sixgroup.refit.observability.item35.creator.shared.utils.LazyIterators;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.common.header.Headers;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static com.sixgroup.refit.observability.item.state.domain.enums.State.*;
import static com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType.COMPUTE_CAPACITY;
import static com.sixgroup.refit.observability.item35.creator.shared.constants.AppConstants.ITEM35_ID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UseCaseComputeCapacity implements ItemTypeStrategy {

    private final CapacityCpuService capacityCpuService;
    private final CapacityRamService capacityRamService;
    private final WriteFileItem35Service<Capacity> writeFileComputeCapacity;
    private final ProducerItemService producerItemService;
    private final FileNameService fileNameService;
    private final StateService stateService;
    private final ItemLog iLog = new ItemLog();

    @Override
    public File execute(final ItemCommandDTO itemCommand, final Headers headers) {

        log.debug("Generating compute capacity file ...");
        final String fileName = fileNameService.getFileName(COMPUTE_CAPACITY, itemCommand.getItemDate());
        File file = null;

        try {
            stateService.nextStep(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).build());

            //Find information
            final String dateFrom = DateUtils.firstDayOfPreviousMonth(itemCommand.getItemDate());
            final String dateTo = DateUtils.firstDayOfCurrentMonth(itemCommand.getItemDate());

            final List<Capacity> capacityCpu = capacityCpuService.findByCapacityCpu(dateFrom, dateTo);
            final List<Capacity> capacityRam = capacityRamService.findByCapacityRam(dateFrom, dateTo);

            if (CollectionUtils.isEmpty(capacityCpu) || CollectionUtils.isEmpty(capacityRam)) {
                iLog.info(ItemReportingDto.builder().itemType(ITEM35_ID).build(), ERROR);
                log.error("No data found in compute capacity, skipping report generation");
                stateService.setError(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).errorDescription("No record status found, skipping file generation").build());
                return null;
            }

            //Saving information
            stateService.nextStep(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).build());
            iLog.info(ItemReportingDto.builder().itemType(ITEM35_ID).build(), SAVING_INFORMATION);

            //Saved information
            final Iterator<Capacity> recordsCapacity = LazyIterators.mergeSorted(
                Comparator.comparing(Capacity::getDate), capacityCpu.iterator(), capacityRam.iterator());

            file = writeFileComputeCapacity.writeFileStreaming(recordsCapacity, itemCommand, fileName);
            stateService.nextStep(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).fileUrl(file.getPath()).build());
            iLog.info(ItemReportingDto.builder().itemType(ITEM35_ID).build(), SAVED_INFORMATION);

            //Sent response
            itemCommand.setFileName(file.getName());
            itemCommand.setFileUrl(file.getAbsolutePath());
            producerItemService.send(itemCommand, headers);
            stateService.nextStep(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).build());
            iLog.info(ItemReportingDto.builder().itemType(ITEM35_ID).build(), SENT_RESPONSE);
        } catch (Exception e) {
            iLog.info(ItemReportingDto.builder().itemType(ITEM35_ID).build(), ERROR);
            log.error("Error to generate file compute capacity {}", e.getMessage(), e);
            stateService.setError(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).errorDescription("Error to generate file compute capacity: " + e.getMessage()).build());
        }
        return file;
    }

    @Override
    public ItemType getItemType() {
        return COMPUTE_CAPACITY;
    }
}
