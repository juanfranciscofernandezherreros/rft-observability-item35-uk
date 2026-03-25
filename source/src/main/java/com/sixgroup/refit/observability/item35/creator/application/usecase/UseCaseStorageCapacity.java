package com.sixgroup.refit.observability.item35.creator.application.usecase;

import com.sixgroup.refit.observability.item.log.ItemLog;
import com.sixgroup.refit.observability.item.state.application.StateService;
import com.sixgroup.refit.observability.item.state.domain.model.ItemReportingDto;
import com.sixgroup.refit.observability.item.state.domain.model.StateRequest;
import com.sixgroup.refit.observability.item35.creator.application.service.FileNameService;
import com.sixgroup.refit.observability.item35.creator.application.service.StorageService;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.model.Storage;
import com.sixgroup.refit.observability.item35.creator.domain.model.StorageCapacityDto;
import com.sixgroup.refit.observability.item35.creator.domain.service.ProducerItemService;
import com.sixgroup.refit.observability.item35.creator.domain.service.WriteFileItem35Service;
import com.sixgroup.refit.observability.item35.creator.domain.strategy.ItemTypeStrategy;
import com.sixgroup.refit.observability.item35.creator.shared.utils.DateUtils;
import com.sixgroup.refit.observability.item35.creator.shared.utils.MathsUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.common.header.Headers;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.sixgroup.refit.observability.item.state.domain.enums.State.*;
import static com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType.STORAGE_CAPACITY;
import static com.sixgroup.refit.observability.item35.creator.shared.constants.AppConstants.ITEM35_ID;
import static com.sixgroup.refit.observability.item35.creator.shared.constants.AppConstants.NUM_DECIMALS;

@Service
@RequiredArgsConstructor
@Slf4j
public class UseCaseStorageCapacity implements ItemTypeStrategy {

    private final WriteFileItem35Service<StorageCapacityDto> writeFileStorageCapacityService;
    private final ProducerItemService producerItemService;
    private final StateService stateService;
    private final FileNameService fileNameService;
    private final StorageService storageService;
    private final ItemLog iLog = new ItemLog();

    @Override
    public File execute(ItemCommandDTO itemCommand, Headers headers) {

        final ItemType itemType = getItemType();
        final String itemId = itemCommand.getItemId();
        final String itemDate = itemCommand.getItemDate();
        final String fileName = fileNameService.getFileName(itemType, itemDate);

        log.info("Starting processing for itemType: {} itemId: {} itemDate: {}", itemType, itemId, itemDate);
        log.debug("Generated fileName: {}", fileName);

        File file = null;

        try {

            stateService.nextStep(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).build());

            log.info("Fetching storage capacity data for period calculation...");

            final String dateFrom = DateUtils.firstDayOfPreviousMonth(itemDate);
            final String dateTo = DateUtils.firstDayOfCurrentMonth(itemDate);

            log.debug("Date range calculated -> from: {} to: {}", dateFrom, dateTo);

            final List<Storage> totalCapacityList = storageService.getTotalCapacity(dateFrom, dateTo);
            final List<Storage> totalFreeCapacityList = storageService.getTotalFreeCapacity(dateFrom, dateTo);

            log.info("Data retrieved -> totalCapacityRecords: {} totalFreeCapacityRecords: {}", totalCapacityList.size(), totalFreeCapacityList.size());

            if (CollectionUtils.isEmpty(totalCapacityList) || CollectionUtils.isEmpty(totalFreeCapacityList)) {
                log.error("No data found for storage capacity calculation. totalCapacityListEmpty={} totalFreeCapacityListEmpty={}", CollectionUtils.isEmpty(totalCapacityList), CollectionUtils.isEmpty(totalFreeCapacityList));
                iLog.info(ItemReportingDto.builder().itemType(ITEM35_ID).build(), ERROR);
                stateService.setError(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).errorDescription("No record status found, skipping file generation").build());

                return null;
            }

            log.info("Calculating final storage capacity dataset...");
            final List<StorageCapacityDto> storageCapacityFinalList = calculateFinalList(itemDate, totalCapacityList, totalFreeCapacityList);
            log.info("Calculated storage capacity dataset size: {}", storageCapacityFinalList.size());

            if (CollectionUtils.isEmpty(storageCapacityFinalList)) {
                log.error("No matching timestamps between total capacity and free capacity datasets");
                iLog.info(ItemReportingDto.builder().itemType(ITEM35_ID).build(), ERROR);
                stateService.setError(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).errorDescription("No match between 'total free capacity data' and 'total capacity data'").build());

                return null;
            }

            stateService.nextStep(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).build());
            log.info("Writing storage capacity file: {}", fileName);
            iLog.info(ItemReportingDto.builder().itemType(ITEM35_ID).build(), SAVING_INFORMATION);

            try {
                file = writeFileStorageCapacityService.writeFile(storageCapacityFinalList, itemCommand, fileName);
            } catch (Exception ex) {
                log.error("Error writing file for item type {}", getItemType(), ex);
            }

            log.info("File successfully generated at path: {}", file.getAbsolutePath());
            stateService.nextStep(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).fileUrl(file.getPath()).build());
            iLog.info(ItemReportingDto.builder().itemType(ITEM35_ID).build(), SAVED_INFORMATION);
            log.info("Sending response event to Kafka for itemType {}", itemType);

            itemCommand.setFileName(file.getName());
            itemCommand.setFileUrl(file.getAbsolutePath());

            producerItemService.send(itemCommand, headers);
            stateService.nextStep(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).build());
            iLog.info(ItemReportingDto.builder().itemType(ITEM35_ID).build(), SENT_RESPONSE);
            log.info("Processing finished successfully for itemType {} file {}", itemType, fileName);

        } catch (Exception e) {
            log.error("Error generating storage capacity file for itemType {} itemId {} : {}", itemType, itemId, e.getMessage(), e);
            iLog.info(ItemReportingDto.builder().itemType(ITEM35_ID).build(), ERROR);
            stateService.setError(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).errorDescription("Error generating storage capacity file: " + e.getMessage()).build());

            return null;
        }

        return file;
    }

    private List<StorageCapacityDto> calculateFinalList(String itemDate, List<Storage> totalCapacityList, List<Storage> totalFreeCapacityList) {
        log.debug("Starting capacity calculations...");

        final AtomicReference<BigDecimal> referenceCapacity = new AtomicReference<>(totalCapacityList.get(0).getCapacity());
        final AtomicReference<BigDecimal> referenceAvailableCapacity = new AtomicReference<>(totalFreeCapacityList.get(0).getCapacity());
        final List<StorageCapacityDto> storageCapacityFinalList = new ArrayList<>();

        totalCapacityList.forEach(totalStorage -> totalFreeCapacityList.forEach(totalFreeStorage -> {

            if (totalStorage.getTimeStamp().equals(totalFreeStorage.getTimeStamp())) {
                final StorageCapacityDto storageCapacityDto = new StorageCapacityDto();
                storageCapacityDto.setTimeStamp(totalStorage.getTimeStamp());
                referenceAvailableCapacity.set(totalFreeStorage.getCapacity());

                if (isCapacityIncrease(referenceCapacity.get(), totalStorage.getCapacity())) {
                    if (isFreeCapacityGreaterThanTotal(totalFreeStorage.getCapacity(), totalStorage.getCapacity())) {
                        referenceCapacity.set(totalFreeStorage.getCapacity());
                        referenceAvailableCapacity.set(totalStorage.getCapacity());
                    } else {
                        referenceCapacity.set(totalStorage.getCapacity());
                    }
                }

                storageCapacityDto.setCapacity(referenceCapacity.get().setScale(NUM_DECIMALS, RoundingMode.HALF_UP));
                storageCapacityDto.setAvailableCapacity(referenceAvailableCapacity.get().setScale(NUM_DECIMALS, RoundingMode.HALF_UP));

                final BigDecimal usedCapacity = storageCapacityDto.getCapacity().subtract(storageCapacityDto.getAvailableCapacity());
                storageCapacityDto.setUsedCapacity(usedCapacity.setScale(NUM_DECIMALS, RoundingMode.HALF_UP));

                final BigDecimal utilization = storageCapacityDto.getUsedCapacity().divide(storageCapacityDto.getCapacity(), NUM_DECIMALS, RoundingMode.HALF_UP);
                storageCapacityDto.setUtilization(utilization.setScale(NUM_DECIMALS, RoundingMode.HALF_UP));

                String date = DateUtils.createFileDateFromTimeStamp(storageCapacityDto.getTimeStamp());
                storageCapacityDto.setDate(date);

                String itemDateFormatted = DateUtils.itemDateFormatted(itemDate);
                storageCapacityDto.setReportingDate(itemDateFormatted);

                storageCapacityFinalList.add(storageCapacityDto);
            }
        }));

        log.debug("Capacity calculation finished. Records generated: {}", storageCapacityFinalList.size());

        return storageCapacityFinalList;
    }

    @Override
    public ItemType getItemType() {
        return STORAGE_CAPACITY;
    }

    private boolean isCapacityIncrease(BigDecimal referenceCapacity, BigDecimal newCapacity) {
        return MathsUtils.isIntoMayorPercent(referenceCapacity, newCapacity);
    }

    private boolean isFreeCapacityGreaterThanTotal(BigDecimal freeCapacity, BigDecimal totalCapacity) {
        return freeCapacity.compareTo(totalCapacity) > 0;
    }
}
