package com.sixgroup.refit.observability.item35.creator.application.usecase;

import com.sixgroup.refit.observability.item.log.ItemLog;
import com.sixgroup.refit.observability.item.state.application.StateService;
import com.sixgroup.refit.observability.item.state.domain.model.StateRequest;
import com.sixgroup.refit.observability.item35.creator.application.service.StorageService;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.model.Storage;
import com.sixgroup.refit.observability.item35.creator.domain.model.StorageCapacityDto;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static com.sixgroup.refit.observability.item.state.domain.enums.State.SAVING_INFORMATION;
import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.ITEM35;
import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.NUM_DECIMALS;

@Service
@RequiredArgsConstructor
@Slf4j
public class UseCaseStorageCapacity implements ItemTypeStrategy {

    private final WriteFileItem35Service<StorageCapacityDto> writeFileStorageCapacityService;
    private final ProducerItemService producerItemService;
    private final StateService stateService;
    private final StorageService storageService;
    private final ItemLog iLog = new ItemLog();

    @Override
    public File execute(ItemCommandDTO itemCommandDTO, Headers headers) {
        log.debug("Generating storage capacity file ...");
        File file;
        try {
            final String firstDayOfMonth = DateUtils.firstDayOfMonth(itemCommandDTO.getItemDate());
            final String firstDayOfNextMonth = DateUtils.firstDayOfNextMonth(itemCommandDTO.getItemDate());

            final List<Storage> totalCapacityList = storageService.getTotalCapacity(firstDayOfMonth, firstDayOfNextMonth);
            final List<Storage> totalFreeCapacityList = storageService.getTotalFreeCapacity(firstDayOfMonth, firstDayOfNextMonth);

            if (CollectionUtils.isEmpty(totalCapacityList) || CollectionUtils.isEmpty(totalFreeCapacityList)) {
                log.error("No data found in storage capacity, skipping report generation");
                stateService.setError(
                    StateRequest.builder()
                        .fileName(FileUtils.getFileName(itemCommandDTO))
                        .itemType(ITEM35)
                        .errorDescription("No record status found, skipping file generation")
                        .build());
                return null;
            }

            final List<StorageCapacityDto> storageCapacityFinalList = calculateFinalList(itemCommandDTO.getItemDate(),
                totalCapacityList, totalFreeCapacityList);
            if (CollectionUtils.isEmpty(storageCapacityFinalList)) {
                log.error("Not exist any match between 'total free capacity data' and 'total capacity data'");
                stateService.setError(
                    StateRequest.builder()
                        .fileName(FileUtils.getFileName(itemCommandDTO))
                        .itemType(ITEM35)
                        .errorDescription("Not exist any match between 'total free capacity data' and 'total capacity data'")
                        .build());
                return null;
            }

            stateService.nextStep(
                StateRequest.builder().fileName(FileUtils.getFileName(itemCommandDTO)).itemType(ITEM35).build());
            iLog.info(itemCommandDTO, SAVING_INFORMATION);

            file = writeFileStorageCapacityService.writeFile(storageCapacityFinalList, itemCommandDTO);
            log.debug("Generated storage capacity file");
            itemCommandDTO.setFileUrl(file.toString());
            itemCommandDTO.setFileName(file.getName());
            producerItemService.send(itemCommandDTO, headers);
        } catch (Exception e) {
            log.error("Error to generate file storage capacity: {}", e.getMessage(), e);
            stateService.setError(
                StateRequest.builder()
                    .fileName(FileUtils.getFileName(itemCommandDTO))
                    .itemType(ITEM35)
                    .errorDescription("Error to generate file storage capacity: " + e.getMessage())
                    .build());
            return null;
        }
        return file;
    }

    private List<StorageCapacityDto> calculateFinalList(String itemDate, List<Storage> totalCapacityList,
                                                        List<Storage> totalFreeCapacityList) {
        List<StorageCapacityDto> storageCapacityFinalList = new ArrayList<>();
        totalCapacityList.forEach(totalStorage ->
            totalFreeCapacityList.forEach(totalFreeStorage -> {
                if (totalStorage.getTimeStamp().equals(totalFreeStorage.getTimeStamp())) {
                    final StorageCapacityDto storageCapacityDto = new StorageCapacityDto();
                    storageCapacityDto.setTimeStamp(totalStorage.getTimeStamp());
                    storageCapacityDto.setCapacity(BigDecimal.valueOf(totalStorage.getCapacity()).setScale(NUM_DECIMALS, RoundingMode.HALF_UP).floatValue());
                    storageCapacityDto.setAvailableCapacity(BigDecimal.valueOf(totalFreeStorage.getCapacity()).setScale(NUM_DECIMALS, RoundingMode.HALF_UP).floatValue());
                    // CALCULATED VALUES
                    float usedCapacity = storageCapacityDto.getCapacity() - storageCapacityDto.getAvailableCapacity();
                    storageCapacityDto.setUsedCapacity(BigDecimal.valueOf(usedCapacity).setScale(NUM_DECIMALS, RoundingMode.HALF_UP).floatValue());
                    float utilization = storageCapacityDto.getUsedCapacity() / storageCapacityDto.getCapacity();
                    storageCapacityDto.setUtilization(BigDecimal.valueOf(utilization).setScale(NUM_DECIMALS, RoundingMode.HALF_UP).floatValue());
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
        return ItemType.STORAGE_CAPACITY;
    }


}
