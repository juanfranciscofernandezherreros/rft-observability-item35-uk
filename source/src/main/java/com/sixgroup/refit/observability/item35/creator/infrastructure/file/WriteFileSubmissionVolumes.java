package com.sixgroup.refit.observability.item35.creator.infrastructure.file;

import com.sixgroup.refit.observability.item35.creator.domain.enums.Command;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemFileFinderRequest;
import com.sixgroup.refit.observability.item35.creator.domain.service.WriteFileItem35Service;
import com.sixgroup.refit.observability.item35.creator.domain.model.RecordStatus;
import com.opencsv.CSVWriter;
import com.sixgroup.refit.observability.item35.creator.shared.Utils;
import com.sixgroup.refit.observability.item35.creator.state.application.StateService;
import com.sixgroup.refit.observability.item35.creator.state.domain.StateRequest;
import com.sixgroup.refit.observability.modules.log.rft.application.RftLog;
import com.sixgroup.refit.observability.modules.log.rft.domain.logobject.base.NameObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.sixgroup.refit.observability.item35.creator.shared.Constants.*;

@Service
@RequiredArgsConstructor
public class WriteFileSubmissionVolumes implements WriteFileItem35Service {

    private final StateService stateService;

    @Override
    public File writeFile(List<RecordStatus> recordStatus, String filePath, String itemDate) throws IOException {
        RftLog.info("Creating and writing file");
        try (FileWriter writer = new FileWriter(filePath);
             CSVWriter csvWriter = new CSVWriter(writer)) {
            writeHeader(csvWriter);
            for (RecordStatus record : recordStatus) {
                writeRecord(csvWriter, record);
            }
        }
        RftLog.info("File created and written: " + filePath);
        stateService.nextStep(
            StateRequest.builder().fileName(Utils.getFileName(itemDate)).itemType(ITEM35).fileUrl(filePath).build());
        File file = new File(filePath);
        RftLog.info("Save file", () ->
            List.of(NameObject.builder().name("timestamp").object(LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE)).build(),
                NameObject.builder().name("itemId").object(ITEM35).build(),
                NameObject.builder().name("itemType").object(ItemType.SUBMISSION_VOLUMES.getName()).build(),
                NameObject.builder().name("command").object(Command.REQUEST).build(),
                NameObject.builder().name("fileName").object(file.getName()).build(),
                NameObject.builder().name("fileUrl").object(file.getAbsolutePath()).build()));

        return file;
    }


    private void writeHeader(CSVWriter csvWriter) {
        csvWriter.writeNext(new String[]{HEADER_TR_CODE, HEADER_REPORTING_DATE,
            HEADER_REGULATION_REFERENCE, HEADER_MESSAGE_TYPE, HEADER_SUBMISSION_CHANNEL, HEADER_NO_MESSAGES_ON_GIVE, HEADER_DATE});
    }

    private void writeRecord(CSVWriter csvWriter, RecordStatus record) {
        String[] data = {
            TR_CODE,
            record.reportingDate(),
            EMIR,
            record.messageType(),
            record.submissionChannel(),
            String.valueOf(record.noMessagesOnGiveDate()),
            LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT_yyyy_MM_dd))
        };
        csvWriter.writeNext(data);
    }

}

