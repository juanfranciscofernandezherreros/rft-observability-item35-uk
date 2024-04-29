package com.sixgroup.refit.observability.item35.creator.application.usecase;

import com.sixgroup.refit.observability.item.state.application.StateService;
import com.sixgroup.refit.observability.item.state.domain.model.StateRequest;
import com.sixgroup.refit.observability.item35.creator.application.service.LogService;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.model.Storage;
import com.sixgroup.refit.observability.item35.creator.domain.model.StorageCapacityDto;
import com.sixgroup.refit.observability.item35.creator.domain.service.ProducerItemService;
import com.sixgroup.refit.observability.item35.creator.domain.service.StorageService;
import com.sixgroup.refit.observability.item35.creator.domain.service.WriteFileItem35Service;
import com.sixgroup.refit.observability.item35.creator.domain.strategy.ItemTypeStrategy;
import com.sixgroup.refit.observability.item35.creator.shared.utils.Utils;
import com.sixgroup.refit.observability.modules.log.rft.application.RftLog;
import com.sixgroup.refit.observability.modules.log.rft.domain.logobject.base.NameObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.apache.kafka.common.header.Headers;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.*;
import static com.sixgroup.refit.observability.item35.creator.shared.utils.Utils.createFileDateFromTimeStamp;
import static com.sixgroup.refit.observability.item35.creator.shared.utils.Utils.getItemDateFormatted;

@Service
@RequiredArgsConstructor
@Slf4j
public class UseCaseStorageCapacity implements ItemTypeStrategy {


    private final WriteFileItem35Service<StorageCapacityDto> writeFileStorageCapacityService;

    private final ProducerItemService producerItemService;

    private final StateService stateService;

    private final StorageService storageService;

    @Override
    public File execute(ItemCommandDTO itemCommandDTO, Headers headers) {
        log.debug("Generating storage capacity file ...");
        File fileStorageCapacity;
        try {
            stateService.nextStep(
                StateRequest.builder().fileName(Utils.getFileName(itemCommandDTO)).itemType(ITEM35).build());
            LogService.logInfo(CREATING_AND_SAVING_FILE, itemCommandDTO);

            String firstDayOfMonth = Utils.getFirstDayOfMonthAndYear(itemCommandDTO.getItemDate());
            String firstDayOfNextMonth = Utils.getFirstDayOfNextMonthAndYear(itemCommandDTO.getItemDate());

            List<Storage> totalCapacityList = storageService.getTotalCapacity(firstDayOfMonth, firstDayOfNextMonth);

            if (CollectionUtils.isEmpty(totalCapacityList)) {
                log.debug("Not exist 'total capacity data' between {} and {}", firstDayOfMonth, firstDayOfNextMonth);
                throw new
                    ResourceNotFoundException("Not exist 'total capacity data' between " + firstDayOfMonth + " and " + firstDayOfNextMonth);
            }

            List<Storage> totalFreeCapacityList = storageService.getTotalFreeCapacity(firstDayOfMonth, firstDayOfNextMonth);

            if (CollectionUtils.isEmpty(totalFreeCapacityList)) {
                log.debug("Not exist 'total free capacity data' between {} and {}", firstDayOfMonth, firstDayOfNextMonth);
                throw new
                    ResourceNotFoundException("Not exist 'total free capacity data' between " + firstDayOfMonth + " and " + firstDayOfNextMonth);
            }

            List<StorageCapacityDto> storageCapacityFinalList = calculateFinalList(itemCommandDTO.getItemDate(),
                totalCapacityList, totalFreeCapacityList);

            if (CollectionUtils.isEmpty(storageCapacityFinalList)) {
                log.debug("Not exist any match between 'total free capacity data' and 'total capacity data'");
                throw new
                    ResourceNotFoundException("Not exist any match between 'total free capacity data' and 'total capacity data'");
            }

            fileStorageCapacity = writeFileStorageCapacityService.writeFile(storageCapacityFinalList, itemCommandDTO);
            log.debug("Generated storage capacity file");
            itemCommandDTO.setFileUrl(fileStorageCapacity.toString());
            itemCommandDTO.setFileName(fileStorageCapacity.getName());
            producerItemService.send(itemCommandDTO, headers);
            return fileStorageCapacity;
        } catch (Exception e) {
            log.error("Error to generate file storage capacity: {}", e.getMessage(), e);
            stateService.setError(
                StateRequest.builder()
                    .fileName(Utils.getFileName(itemCommandDTO))
                    .itemType(ITEM35)
                    .errorDescription("Error to generate file storage capacity: " + e.getMessage())
                    .build());
            return null;
        }
    }

    @Override
    public ItemType getItemType() {
        return ItemType.STORAGE_CAPACITY;
    }

    private static List<StorageCapacityDto> calculateFinalList(String itemDate, List<Storage> totalCapacityList,
                                                               List<Storage> totalFreeCapacityList) {
        List<StorageCapacityDto> storageCapacityFinalList = new ArrayList<>();
        totalCapacityList.forEach(totalStorage ->
            totalFreeCapacityList.forEach(totalFreeStorage -> {
                if (totalStorage.getTimeStamp().equals(totalFreeStorage.getTimeStamp())) {
                    StorageCapacityDto storageCapacityDto = new StorageCapacityDto();
                    storageCapacityDto.setTimeStamp(totalStorage.getTimeStamp());
                    storageCapacityDto.setCapacity(BigDecimal.valueOf(totalStorage.getCapacity()).setScale(FOUR_DECIMALS, RoundingMode.HALF_UP).floatValue());
                    storageCapacityDto.setAvailableCapacity(BigDecimal.valueOf(totalFreeStorage.getCapacity()).setScale(FOUR_DECIMALS, RoundingMode.HALF_UP).floatValue());
                    // CALCULATED VALUES
                    float usedCapacity = storageCapacityDto.getCapacity() - storageCapacityDto.getAvailableCapacity();
                    storageCapacityDto.setUsedCapacity(BigDecimal.valueOf(usedCapacity).setScale(FOUR_DECIMALS, RoundingMode.HALF_UP).floatValue());
                    float utilization = storageCapacityDto.getUsedCapacity() / storageCapacityDto.getCapacity();
                    storageCapacityDto.setUtilization(BigDecimal.valueOf(utilization).setScale(FOUR_DECIMALS, RoundingMode.HALF_UP).floatValue());
                    String date = createFileDateFromTimeStamp(storageCapacityDto.getTimeStamp());
                    storageCapacityDto.setDate(date);

                    String itemDateFormatted = getItemDateFormatted(itemDate);
                    storageCapacityDto.setReportingDate(itemDateFormatted);

                    storageCapacityFinalList.add(storageCapacityDto);
                }
            }));
        return storageCapacityFinalList;
    }

}
