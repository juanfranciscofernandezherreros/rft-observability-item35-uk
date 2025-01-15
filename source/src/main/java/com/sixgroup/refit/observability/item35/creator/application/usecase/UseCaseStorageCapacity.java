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
        log.debug("Generating storage capacity file ...");
        final String fileName = fileNameService.getFileName(STORAGE_CAPACITY, itemCommand.getItemDate());
        File file;
        try {
            stateService.nextStep(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).build());

            //Find information
            final String dateFrom = DateUtils.firstDayOfPreviousMonth(itemCommand.getItemDate());
            final String dateTo = DateUtils.firstDayOfCurrentMonth(itemCommand.getItemDate());

            final List<Storage> totalCapacityList = storageService.getTotalCapacity(dateFrom, dateTo);
            final List<Storage> totalFreeCapacityList = storageService.getTotalFreeCapacity(dateFrom, dateTo);

            if (CollectionUtils.isEmpty(totalCapacityList) || CollectionUtils.isEmpty(totalFreeCapacityList)) {
                iLog.info(ItemReportingDto.builder().itemType(ITEM35_ID).build(), ERROR);
                log.error("No data found in storage capacity, skipping report generation");
                stateService.setError(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).errorDescription("No record status found, skipping file generation").build());
                return null;
            }

            final List<StorageCapacityDto> storageCapacityFinalList = calculateFinalList(itemCommand.getItemDate(),
                totalCapacityList, totalFreeCapacityList);
            if (CollectionUtils.isEmpty(storageCapacityFinalList)) {
                iLog.info(ItemReportingDto.builder().itemType(ITEM35_ID).build(), ERROR);
                log.error("Not exist any match between 'total free capacity data' and 'total capacity data'");
                stateService.setError(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).errorDescription("Not exist any match between 'total free capacity data' and 'total capacity data'").build());
                return null;
            }

            //Saving information
            stateService.nextStep(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).build());
            iLog.info(itemCommand, SAVING_INFORMATION);

            //Saved information
            file = writeFileStorageCapacityService.writeFile(storageCapacityFinalList, itemCommand, fileName);
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
            log.error("Error to generate file storage capacity: {}", e.getMessage(), e);
            stateService.setError(StateRequest.builder().fileName(fileName).itemType(ITEM35_ID).errorDescription("Error to generate file storage capacity: " + e.getMessage()).build());
            return null;
        }
        return file;
    }

    private List<StorageCapacityDto> calculateFinalList(String itemDate, List<Storage> totalCapacityList,
                                                        List<Storage> totalFreeCapacityList) {
        final AtomicReference<BigDecimal> referenceCapacity = new AtomicReference<>(totalCapacityList.get(0).getCapacity());
        final AtomicReference<BigDecimal> referenceAvailableCapacity = new AtomicReference<>(totalFreeCapacityList.get(0).getCapacity());

        final List<StorageCapacityDto> storageCapacityFinalList = new ArrayList<>();
        totalCapacityList.forEach(totalStorage ->
            totalFreeCapacityList.forEach(totalFreeStorage -> {
                if (totalStorage.getTimeStamp().equals(totalFreeStorage.getTimeStamp())) {
                    final StorageCapacityDto storageCapacityDto = new StorageCapacityDto();
                    storageCapacityDto.setTimeStamp(totalStorage.getTimeStamp());

                    //Capacity
                    referenceAvailableCapacity.set(totalFreeStorage.getCapacity());
                    if (MathsUtils.isIntoMayorPercent(referenceCapacity.get(), totalStorage.getCapacity())) {
                        if (totalFreeStorage.getCapacity().compareTo(totalStorage.getCapacity()) > 0) {
                            referenceCapacity.set(totalFreeStorage.getCapacity());
                            referenceAvailableCapacity.set(totalStorage.getCapacity());
                        } else {
                            referenceCapacity.set(totalStorage.getCapacity());
                        }
                    }
                    storageCapacityDto.setCapacity(referenceCapacity.get().setScale(NUM_DECIMALS, RoundingMode.HALF_UP));
                    storageCapacityDto.setAvailableCapacity(referenceAvailableCapacity.get().setScale(NUM_DECIMALS, RoundingMode.HALF_UP));

                    // CALCULATED VALUES
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
        return storageCapacityFinalList;
    }

    @Override
    public ItemType getItemType() {
        return STORAGE_CAPACITY;
    }
}
