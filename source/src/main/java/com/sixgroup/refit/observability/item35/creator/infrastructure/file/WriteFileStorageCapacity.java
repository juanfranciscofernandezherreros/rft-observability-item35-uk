package com.sixgroup.refit.observability.item35.creator.infrastructure.file;

import com.opencsv.CSVWriter;
import com.sixgroup.refit.observability.item35.creator.configuration.CsvProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.Regulation;
import com.sixgroup.refit.observability.item35.creator.configuration.ReportItemProperties;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.model.StorageCapacityDto;
import com.sixgroup.refit.observability.item35.creator.domain.service.WriteFileItem35Service;
import com.sixgroup.refit.observability.item35.creator.shared.csv.CSVCreator;
import com.sixgroup.refit.observability.item35.creator.shared.utils.MathsUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.AppConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WriteFileStorageCapacity implements WriteFileItem35Service<StorageCapacityDto> {

    private final CsvProperties csvProperties;
    private final ReportItemProperties reportItemProperties;

    @Override
    public File writeFileStreaming(final Iterator<StorageCapacityDto> storageCapacityDtoList,
                          final ItemCommandDTO itemCommandDTO,
                          final String fileName) throws IOException {

        log.debug("Determining output directory based on regulation");

        Path targetPath = java.nio.file.Path.of(csvProperties.getOutputPath());
        log.info("TargetPath {} :", targetPath);

        Files.createDirectories(targetPath);
        java.nio.file.Path finalFile = targetPath.resolve(fileName);
        log.info("Target file path: {}", finalFile);

        try (FileWriter writer = new FileWriter(finalFile.toFile());
             CSVWriter csvWriter = CSVCreator.create(writer)) {
            writeHeader(csvWriter);

            while (storageCapacityDtoList.hasNext()) {
                StorageCapacityDto storageCapacityData = storageCapacityDtoList.next();
                writeRecord(csvWriter, storageCapacityData);
            }
        }

        log.info("File created and written successfully at: {}", finalFile);
        return finalFile.toFile();
    }

    private void writeHeader(final CSVWriter csvWriter) {
        String[] headers = ItemType.STORAGE_CAPACITY.getHeadersWithIncidentId(reportItemProperties.getIncidentIdHeader());
        csvWriter.writeNext(headers);
    }

    private void writeRecord(final CSVWriter csvWriter, final StorageCapacityDto storageCapacityData) {
        String[] data = {
            reportItemProperties.getTrCode(),
            storageCapacityData.getReportingDate(),
            //REFIT-7169: UK ITEM35C must write EMIR in the Regulation field instead of FCA.
            reportItemProperties.getRegulationReference(),
            DATA_CENTER_LOCATION,
            DATABASE_SERVER_OR_PLATFORM,
            storageCapacityData.getDate(),
            MathsUtils.formatBigDecimalToFourDecimals(storageCapacityData.getCapacity()),
            MathsUtils.formatBigDecimalToFourDecimals(storageCapacityData.getUsedCapacity()),
            MathsUtils.formatBigDecimalToFourDecimals(storageCapacityData.getAvailableCapacity()),
            MathsUtils.formatBigDecimalToFourDecimals(storageCapacityData.getUtilization()),
            INCIDENT_RELATED,
            TR_INCIDENT_ID
        };
        csvWriter.writeNext(data);
    }
}
