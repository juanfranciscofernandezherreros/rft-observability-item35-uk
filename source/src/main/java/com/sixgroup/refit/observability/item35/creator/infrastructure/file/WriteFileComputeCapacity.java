package com.sixgroup.refit.observability.item35.creator.infrastructure.file;

import com.opencsv.CSVWriter;
import com.sixgroup.refit.observability.item35.creator.configuration.CsvProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.Regulation; // Asegúrate de importar esto
import com.sixgroup.refit.observability.item35.creator.configuration.ReportItemProperties;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.Capacity;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.service.WriteFileItem35Service;
import com.sixgroup.refit.observability.item35.creator.shared.csv.CSVCreator;
import com.sixgroup.refit.observability.item35.creator.shared.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.AppConstants.DATABASE_SERVER_OR_PLATFORM;
import static com.sixgroup.refit.observability.item35.creator.shared.constants.AppConstants.DATA_CENTER_LOCATION;
import static com.sixgroup.refit.observability.item35.creator.shared.constants.CapacityConstants.FIELD_INCIDENT_RELATED_FILE;
import static com.sixgroup.refit.observability.item35.creator.shared.constants.CapacityConstants.FIELD_TR_INCIDENT_ID_RELATED_FILE;

@Service
@RequiredArgsConstructor
@Slf4j
public class WriteFileComputeCapacity implements WriteFileItem35Service<Capacity> {

    private final CsvProperties csvProperties;
    private final ReportItemProperties reportItemProperties;

    @Override
    public File writeFile(final List<Capacity> capacities,
                          final ItemCommandDTO itemCommandDTO,
                          final String fileName) throws IOException {

        log.debug("Determining output directory based on regulation for Compute Capacity");

        // 1. Construcción de ruta segura con Path
        Path targetPath = Path.of(csvProperties.getOutputPath());
        log.info("Target path: {}", targetPath);

        // 2. Crear directorios si no existen
        Files.createDirectories(targetPath);

        Path finalFile = targetPath.resolve(fileName);
        log.info("Target file path: {}", finalFile);

        // 3. Escritura del CSV
        try (FileWriter writer = new FileWriter(finalFile.toFile());
             CSVWriter csvWriter = CSVCreator.create(writer)) {

            writeHeader(csvWriter);

            for (Capacity capacityData : capacities) {
                writeRecord(csvWriter, capacityData, itemCommandDTO.getItemDate());
            }
        }

        log.info("File created and written successfully at: {}", finalFile);
        return finalFile.toFile();
    }

    private void writeHeader(final CSVWriter csvWriter) {
        String[] headers = ItemType.COMPUTE_CAPACITY.getHeadersWithIncidentId(reportItemProperties.getIncidentIdHeader());
        csvWriter.writeNext(headers);
    }

    private void writeRecord(final CSVWriter csvWriter, final Capacity capacityData, final String itemDate) {
        String[] data = {
            reportItemProperties.getTrCode(),
            DateUtils.itemDateFormatted(itemDate),
            reportItemProperties.getRegulationReference(),
            DATA_CENTER_LOCATION,
            DATABASE_SERVER_OR_PLATFORM,
            capacityData.getTypeCapacity(),
            capacityData.getDate(),
            capacityData.getMin(),
            capacityData.getMean(),
            capacityData.getMax(),
            FIELD_INCIDENT_RELATED_FILE,
            FIELD_TR_INCIDENT_ID_RELATED_FILE
        };
        csvWriter.writeNext(data);
    }
}
