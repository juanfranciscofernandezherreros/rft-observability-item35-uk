package com.sixgroup.refit.observability.item35.creator.infrastructure.file;

import com.opencsv.CSVWriter;
import com.sixgroup.refit.observability.item35.creator.configuration.CsvProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.ReportProperties;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.Capacity;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.service.WriteFileItem35Service;
import com.sixgroup.refit.observability.item35.creator.shared.csv.CSVCreator;
import com.sixgroup.refit.observability.item35.creator.shared.utils.DateUtils;
import com.sixgroup.refit.observability.item35.creator.shared.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    private final ReportProperties reportProperties;

    @Override
    public File writeFile(final List<Capacity> capacities, final ItemCommandDTO itemCommandDTO) throws IOException {
        log.debug("Creating and writing file");
        String filePath = csvProperties.getOutputPath() + FileUtils.getFileName(itemCommandDTO);
        try (FileWriter writer = new FileWriter(filePath);
             CSVWriter csvWriter = CSVCreator.create(writer)) {
            writeHeader(csvWriter);
            for (Capacity capacityData : capacities) {
                writeRecord(csvWriter, capacityData, itemCommandDTO.getItemDate());
            }
        }
        log.debug("File created and written: " + filePath);
        return new File(filePath);
    }

    private void writeHeader(final CSVWriter csvWriter) {
        csvWriter.writeNext(ItemType.COMPUTE_CAPACITY.getHeaders());
    }


    private void writeRecord(final CSVWriter csvWriter, final Capacity capacityData, final String itemDate) {
        String[] data = {
            reportProperties.getTrCode(),
            DateUtils.itemDateFormatted(itemDate),
            reportProperties.getRegulationReference(),
            DATA_CENTER_LOCATION,
            DATABASE_SERVER_OR_PLATFORM,
            capacityData.getTypeCapacity(),
            capacityData.getDate(),
            capacityData.getMin(),
            capacityData.getMean(),
            capacityData.getMax(),
            FIELD_INCIDENT_RELATED_FILE,
            FIELD_TR_INCIDENT_ID_RELATED_FILE};
        csvWriter.writeNext(data);
    }


}
