package com.sixgroup.refit.observability.item35.creator.infrastructure.file;

import com.opencsv.CSVWriter;
import com.sixgroup.refit.observability.item.state.application.StateService;
import com.sixgroup.refit.observability.item.state.domain.model.StateRequest;
import com.sixgroup.refit.observability.item35.creator.application.service.LogService;
import com.sixgroup.refit.observability.item35.creator.configuration.CsvProperties;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.Capacity;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.service.WriteFileItem35Service;
import com.sixgroup.refit.observability.item35.creator.shared.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.CapacityConstants.*;
import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class WriteFileComputeCapacity implements WriteFileItem35Service<Capacity> {

    private final CsvProperties csvProperties;

    private final StateService stateService;


    @Override
    public File writeFile(List<Capacity> records, ItemCommandDTO itemCommandDTO) throws IOException {
        log.debug("Creating and writing file");
        String filePath = csvProperties.getOutputPath() + Utils.getFileName(itemCommandDTO);
        try (FileWriter writer = new FileWriter(filePath);
             CSVWriter csvWriter = new CSVWriter(writer)) {
            writeHeader(csvWriter);
            for (Capacity record : records) {
                writeRecord(csvWriter, record, itemCommandDTO.getItemDate());
            }
        }
        log.debug("File created and written: " + filePath);
        stateService.nextStep(
            StateRequest.builder().fileName(Utils.getFileName(itemCommandDTO)).itemType(ITEM35).fileUrl(filePath).build());
        return new File(filePath);
    }

    private void writeHeader(CSVWriter csvWriter) {
        csvWriter.writeNext(ItemType.COMPUTE_CAPACITY.getHeaders());
    }


    private void writeRecord(CSVWriter csvWriter, Capacity record, String itemDate) {
        String[] data = {
            TR_CODE,
            Utils.getItemDateFormatted(itemDate),
            EMIR,
            DATA_CENTER_LOCATION,
            DATABASE_SERVER_OR_PLATFORM,
            record.getTypeCapacity(),
            record.getDate(),
            record.getMin(),
            record.getMean(),
            record.getMax(),
            FIELD_INCIDENT_RELATED_FILE,
            FIELD_TR_INCIDENT_ID_RELATED_FILE};
        csvWriter.writeNext(data);
    }


}
