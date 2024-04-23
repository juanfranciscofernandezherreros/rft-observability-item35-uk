package com.sixgroup.refit.observability.item35.creator.infrastructure.file;

import com.opencsv.CSVWriter;
import com.sixgroup.refit.observability.item.state.application.StateService;
import com.sixgroup.refit.observability.item.state.domain.model.StateRequest;
import com.sixgroup.refit.observability.item35.creator.application.service.LogService;
import com.sixgroup.refit.observability.item35.creator.configuration.CsvProperties;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.enums.Status;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.model.RecordStatus;
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

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WriteFileSubmissionVolumes implements WriteFileItem35Service<RecordStatus> {

    private final StateService stateService;

    private final CsvProperties csvProperties;

    @Override
    public File writeFile(List<RecordStatus> recordStatus, ItemCommandDTO itemCommandDTO) throws IOException {
        log.debug("Creating and writing file");
        String filePath = csvProperties.getOutputPath() + Utils.getFileName(itemCommandDTO);
        try (FileWriter writer = new FileWriter(filePath);
             CSVWriter csvWriter = new CSVWriter(writer)) {
            writeHeader(csvWriter);
            for (RecordStatus record : recordStatus) {
                writeRecord(csvWriter, record, itemCommandDTO.getItemDate());
            }
        }
        log.debug("File created and written: " + filePath);
        stateService.nextStep(
            StateRequest.builder().fileName(Utils.getFileName(itemCommandDTO)).itemType(ITEM35).fileUrl(filePath).build());
        return new File(filePath);
    }


    private void writeHeader(CSVWriter csvWriter) {
        csvWriter.writeNext(ItemType.SUBMISSION_VOLUMES.getHeaders());
    }

    private void writeRecord(CSVWriter csvWriter, RecordStatus record, String itemDate) {
        String[] data = {
            TR_CODE,
            Utils.getItemDateFormatted(itemDate),
            EMIR,
            Status.getStatusFromDescription(record.messageType()).toString(),
            record.submissionChannel().toUpperCase(),
            String.valueOf(record.noMessagesOnGiveDate()),
            record.reportingDate()
        };
        csvWriter.writeNext(data);
    }

}

