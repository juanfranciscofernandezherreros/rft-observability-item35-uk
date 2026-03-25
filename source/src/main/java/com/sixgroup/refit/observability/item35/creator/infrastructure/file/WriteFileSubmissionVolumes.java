package com.sixgroup.refit.observability.item35.creator.infrastructure.file;

import com.opencsv.CSVWriter;
import com.sixgroup.refit.observability.item35.creator.configuration.CsvProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.Regulation;
import com.sixgroup.refit.observability.item35.creator.configuration.ReportItemProperties;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.enums.Status;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.model.RecordStatus;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class WriteFileSubmissionVolumes implements WriteFileItem35Service<RecordStatus> {

    private final CsvProperties csvProperties;
    private final ReportItemProperties reportProperties;

    @Override
    public File writeFile(final List<RecordStatus> recordStatus,
                          final ItemCommandDTO itemCommandDTO,
                          final String fileName) throws IOException {

        log.debug("Determining output directory based on regulation for Submission Volumes");

        Path targetPath = Path.of(csvProperties.getOutputPath());
        log.info("Target path: {}", targetPath);

        Files.createDirectories(targetPath);

        Path finalFile = targetPath.resolve(fileName);
        log.info("Target file path: {}", finalFile);

        try (FileWriter writer = new FileWriter(finalFile.toFile());
             CSVWriter csvWriter = CSVCreator.create(writer)) {

            writeHeader(csvWriter);

            for (RecordStatus recordStatusData : recordStatus) {
                writeRecord(csvWriter, recordStatusData, itemCommandDTO.getItemDate());
            }
        }

        log.info("File created and written successfully at: {}", finalFile);
        return finalFile.toFile();
    }

    private void writeHeader(final CSVWriter csvWriter) {
        // Nota: He mantenido getHeaders() tal como estaba en tu original para esta clase
        csvWriter.writeNext(ItemType.SUBMISSION_VOLUMES.getHeaders());
    }

    private void writeRecord(final CSVWriter csvWriter, final RecordStatus recordStatusData, final String itemDate) {
        String[] data = {
            reportProperties.getTrCode(),
            DateUtils.itemDateFormatted(itemDate),
            reportProperties.getRegulationReference(),
            Status.getStatusFromDescription(recordStatusData.messageType()).toString(),
            recordStatusData.submissionChannel().toUpperCase(),
            String.valueOf(recordStatusData.noMessagesOnGiveDate()),
            recordStatusData.reportingDate()
        };
        csvWriter.writeNext(data);
    }
}
