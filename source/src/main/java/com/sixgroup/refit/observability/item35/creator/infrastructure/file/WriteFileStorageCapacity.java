package com.sixgroup.refit.observability.item35.creator.infrastructure.file;

import com.opencsv.CSVWriter;
import com.sixgroup.refit.observability.item35.creator.configuration.CsvProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.ReportProperties;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.model.StorageCapacityDto;
import com.sixgroup.refit.observability.item35.creator.domain.service.WriteFileItem35Service;
import com.sixgroup.refit.observability.item35.creator.shared.csv.CSVCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WriteFileStorageCapacity implements WriteFileItem35Service<StorageCapacityDto> {
    private final CsvProperties csvProperties;
    private final ReportProperties reportProperties;

    @Override
    public File writeFile(final List<StorageCapacityDto> storageCapacityDtoList, final ItemCommandDTO itemCommandDTO) throws IOException {
        log.debug("Creating and writing file");
        String filePath = csvProperties.getOutputPath() + getFileName(itemCommandDTO.getItemDate());
        try (FileWriter writer = new FileWriter(filePath);
             CSVWriter csvWriter = CSVCreator.create(writer)) {
            writeHeader(csvWriter);
            for (StorageCapacityDto storageCapacityData : storageCapacityDtoList) {
                writeRecord(csvWriter, storageCapacityData);
            }
        }
        log.debug("File created and written: {}", filePath);
        return new File(filePath);
    }

    private void writeHeader(final CSVWriter csvWriter) {
        csvWriter.writeNext(ItemType.STORAGE_CAPACITY.getHeaders());
    }


    private void writeRecord(final CSVWriter csvWriter, final StorageCapacityDto storageCapacityData) {
        String[] data = {
            reportProperties.getTrCode(),
            storageCapacityData.getReportingDate(),
            reportProperties.getRegulationReference(),
            DATA_CENTER_LOCATION,
            DATABASE_SERVER_OR_PLATFORM,
            storageCapacityData.getDate(),
            String.valueOf(storageCapacityData.getCapacity()),
            String.valueOf(storageCapacityData.getUsedCapacity()),
            String.valueOf(storageCapacityData.getAvailableCapacity()),
            String.valueOf(storageCapacityData.getUtilization()),
            INCIDENT_RELATED,
            TR_INCIDENT_ID
        };
        csvWriter.writeNext(data);
    }

    private String getFileName(String itemDate) {
        return ItemType.STORAGE_CAPACITY.getNamePattern() + itemDate + ".csv";
    }

}

