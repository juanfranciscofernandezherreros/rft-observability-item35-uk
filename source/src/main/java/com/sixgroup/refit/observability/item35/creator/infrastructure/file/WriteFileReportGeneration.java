package com.sixgroup.refit.observability.item35.creator.infrastructure.file;

import com.opencsv.CSVWriter;
import com.sixgroup.refit.observability.item35.creator.configuration.CsvProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.ReportItemProperties;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.domain.service.WriteFileItem35Service;
import com.sixgroup.refit.observability.item35.creator.shared.csv.CSVCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.AppConstants.TR_INCIDENT_ID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WriteFileReportGeneration implements WriteFileItem35Service<ReportGenerationDto> {

    private final CsvProperties csvProperties;
    private final ReportItemProperties reportProperties;

    @Override
    public File writeFile(final List<ReportGenerationDto> reportGenerationList,
                          final ItemCommandDTO itemCommandDTO,
                          final String fileName) throws IOException {

        log.debug("Determining output directory based on regulation for Report Generation");

        Path targetPath = Path.of(csvProperties.getOutputPath());
        log.info("Target path: {}", targetPath);

        Files.createDirectories(targetPath);

        Path finalFile = targetPath.resolve(fileName);
        log.info("Target file path: {}", finalFile);

        try (FileWriter writer = new FileWriter(finalFile.toFile());
             CSVWriter csvWriter = CSVCreator.create(writer)) {

            writeHeader(csvWriter);

            for (ReportGenerationDto reportGenerationData : reportGenerationList) {
                writeRecord(csvWriter, reportGenerationData);
            }
        }

        log.info("File created and written successfully at: {}", finalFile);
        return finalFile.toFile();
    }

    private void writeHeader(final CSVWriter csvWriter) {
        String[] headers = ItemType.REPORT_GENERATION.getHeadersWithIncidentId(reportProperties.getIncidentIdHeader());
        csvWriter.writeNext(headers);
    }

    private void writeRecord(final CSVWriter csvWriter, final ReportGenerationDto reportGenerationData) {
        String[] data = {
            reportProperties.getTrCode(),
            reportGenerationData.getReportingDate(),
            reportProperties.getRegulationReference(),
            "\"" + reportGenerationData.getReportName() + "\"",
            reportGenerationData.getReportType(),
            reportGenerationData.getReportGenerationTime(),
            reportGenerationData.getReportCompletionTime(),
            reportGenerationData.getReportPublicationTime(),
            reportGenerationData.getDate(),
            reportGenerationData.getSla(),
            reportGenerationData.getDifference(),
            TR_INCIDENT_ID
        };
        csvWriter.writeNext(data);
    }
}
