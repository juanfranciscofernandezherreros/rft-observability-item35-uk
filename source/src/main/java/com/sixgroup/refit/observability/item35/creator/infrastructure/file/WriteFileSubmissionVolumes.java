package com.sixgroup.refit.observability.item35.creator.infrastructure.file;

import com.opencsv.CSVWriter;
import com.sixgroup.refit.observability.item.state.application.StateService;
import com.sixgroup.refit.observability.item.state.domain.model.StateRequest;
import com.sixgroup.refit.observability.item35.creator.configuration.CsvProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.ReportProperties;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.enums.Status;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.model.RecordStatus;
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

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.ITEM35;

@Service
@RequiredArgsConstructor
@Slf4j
public class WriteFileSubmissionVolumes implements WriteFileItem35Service<RecordStatus> {

    private final StateService stateService;
    private final CsvProperties csvProperties;
    private final ReportProperties reportProperties;

    @Override
    public File writeFile(final List<RecordStatus> recordStatus, final ItemCommandDTO itemCommandDTO) throws IOException {
        log.debug("Creating and writing file");
        final String filePath = csvProperties.getOutputPath() + FileUtils.getFileName(itemCommandDTO);
        try (FileWriter writer = new FileWriter(filePath);
             CSVWriter csvWriter = CSVCreator.create(writer)) {
            writeHeader(csvWriter);
            for (RecordStatus record : recordStatus) {
                writeRecord(csvWriter, record, itemCommandDTO.getItemDate());
            }
        }
        log.debug("File created and written: " + filePath);
        stateService.nextStep(
            StateRequest.builder().fileName(FileUtils.getFileName(itemCommandDTO)).itemType(ITEM35).fileUrl(filePath).build());
        return new File(filePath);
    }


    private void writeHeader(CSVWriter csvWriter) {
        csvWriter.writeNext(ItemType.SUBMISSION_VOLUMES.getHeaders());
    }

    private void writeRecord(CSVWriter csvWriter, RecordStatus record, String itemDate) {
        String[] data = {
            reportProperties.getTrCode(),
            DateUtils.itemDateFormatted(itemDate),
            reportProperties.getRegulationReference(),
            Status.getStatusFromDescription(record.messageType()).toString(),
            record.submissionChannel().toUpperCase(),
            String.valueOf(record.noMessagesOnGiveDate()),
            record.reportingDate()
        };
        csvWriter.writeNext(data);
    }

}

