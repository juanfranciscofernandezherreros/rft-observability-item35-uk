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
import com.sixgroup.refit.observability.item35.creator.shared.constants.Constants;
import com.sixgroup.refit.observability.item35.creator.shared.utils.Utils;
import com.sixgroup.refit.observability.modules.log.rft.application.RftLog;
import com.sixgroup.refit.observability.modules.log.rft.domain.logobject.base.NameObject;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.apache.kafka.common.header.Headers;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.*;
import static com.sixgroup.refit.observability.item35.creator.shared.utils.Utils.createFileDateFromTimeStamp;
import static com.sixgroup.refit.observability.item35.creator.shared.utils.Utils.getItemDateFormatted;

@Service
@RequiredArgsConstructor
public class UseCaseStorageCapacity implements ItemTypeStrategy {

    private final WriteFileItem35Service<StorageCapacityDto> writeFileStorageCapacityService;

    private final ProducerItemService producerItemService;

    private final StateService stateService;

    private final StorageService storageService;

    @Override
    public File execute(ItemCommandDTO itemCommandDTO, Headers headers) {
        RftLog.info("Generating storage capacity volumes file ...");
        File fileStorageCapacity;
        try {
            stateService.nextStep(
                StateRequest.builder().fileName(Utils.getFileName(itemCommandDTO)).itemType(ITEM35).build());
            LogService.logInfo(CREATING_AND_SAVING_FILE, itemCommandDTO);

            String firstDayOfMonth = Utils.getFirstDayOfMonthAndYear(itemCommandDTO.getItemDate());
            String firstDayOfNextMonth = Utils.getFirstDayOfNextMonthAndYear(itemCommandDTO.getItemDate());

            List<Storage> totalCapacityList = storageService.getTotalCapacity(firstDayOfMonth, firstDayOfNextMonth);

            if (CollectionUtils.isEmpty(totalCapacityList)) {
                RftLog.info("Not exist 'total capacity data' between " + firstDayOfMonth + " and " + firstDayOfNextMonth);
                throw new
                    ResourceNotFoundException("Not exist 'total capacity data' between " + firstDayOfMonth + " and " + firstDayOfNextMonth);
            }

            List<Storage> totalFreeCapacityList = storageService.getTotalFreeCapacity(firstDayOfMonth, firstDayOfNextMonth);

            if (CollectionUtils.isEmpty(totalFreeCapacityList)) {
                RftLog.info("Not exist 'total free capacity data' between " + firstDayOfMonth + " and " + firstDayOfNextMonth);
                throw new
                    ResourceNotFoundException("Not exist 'total free capacity data' between " + firstDayOfMonth + " and " + firstDayOfNextMonth);
            }

            List<StorageCapacityDto> storageCapacityFinalList = calculateFinalList(itemCommandDTO.getItemDate(),
                totalCapacityList, totalFreeCapacityList);

            if (CollectionUtils.isEmpty(storageCapacityFinalList)) {
                RftLog.info("Not exist any match between 'total free capacity data' and 'total capacity data'");
                throw new
                    ResourceNotFoundException("Not exist any match between 'total free capacity data' and 'total capacity data'");
            }

            fileStorageCapacity = writeFileStorageCapacityService.writeFile(storageCapacityFinalList, itemCommandDTO);
            RftLog.info("Generated storage capacity file");
            itemCommandDTO.setFileUrl(fileStorageCapacity.toString());
            itemCommandDTO.setFileName(fileStorageCapacity.getName());
            producerItemService.send(itemCommandDTO, headers);
            return fileStorageCapacity;
        } catch (Exception e) {
            RftLog.error("Error to generate file storage capacity",
                List.of(NameObject.builder().name("Error").object(e.getMessage()).build()), "");
            stateService.setError(
                StateRequest.builder()
                    .fileName(Utils.getFileName(itemCommandDTO))
                    .itemType(ITEM35)
                    .errorDescription("Error to generate file storage capacity: " + e.getMessage())
                    .build());
            if (e instanceof ResourceNotFoundException) {
                throw new RuntimeException(e.getMessage());
            }
            throw new RuntimeException(e);

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
                    storageCapacityDto.setCapacity(totalStorage.getCapacity());
                    storageCapacityDto.setAvailableCapacity(totalFreeStorage.getCapacity());
                    // CALCULATED VALUES
                    float usedCapacity = storageCapacityDto.getCapacity() - storageCapacityDto.getAvailableCapacity();
                    storageCapacityDto.setUsedCapacity(usedCapacity);
                    float utilization = storageCapacityDto.getUsedCapacity() / storageCapacityDto.getCapacity();
                    storageCapacityDto.setUtilization(utilization);
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
