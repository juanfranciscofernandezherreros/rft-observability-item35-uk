package com.sixgroup.refit.observability.item35.creator.infrastructure.file;

import com.opencsv.CSVWriter;
import com.sixgroup.refit.observability.item.state.application.StateService;
import com.sixgroup.refit.observability.item.state.domain.model.StateRequest;
import com.sixgroup.refit.observability.item35.creator.configuration.CsvProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.ReportProperties;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.domain.service.WriteFileItem35Service;
import com.sixgroup.refit.observability.item35.creator.shared.csv.CSVCreator;
import com.sixgroup.refit.observability.item35.creator.shared.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.ITEM35;
import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.TR_INCIDENT_ID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WriteFileReportGeneration implements WriteFileItem35Service<ReportGenerationDto> {

    private final StateService stateService;
    private final CsvProperties csvProperties;
    private final ReportProperties reportProperties;

    @Override
    public File writeFile(final List<ReportGenerationDto> reportGenerationList, final ItemCommandDTO itemCommandDTO) throws IOException {
        log.debug("Creating and writing file");
        String filePath = csvProperties.getOutputPath() + getFileName(itemCommandDTO.getItemDate());
        try (FileWriter writer = new FileWriter(filePath);
             CSVWriter csvWriter = CSVCreator.create(writer)) {
            writeHeader(csvWriter);
            for (ReportGenerationDto record : reportGenerationList) {
                writeRecord(csvWriter, record);
            }
        }
        log.debug("File created and written: {}", filePath);
        stateService.nextStep(
            StateRequest.builder()
                .fileName(FileUtils.getFileName(itemCommandDTO))
                .itemType(ITEM35).fileUrl(filePath)
                .build());
        return new File(filePath);
    }

    private void writeHeader(CSVWriter csvWriter) {
        csvWriter.writeNext(ItemType.REPORT_GENERATION.getHeaders());
    }


    private void writeRecord(CSVWriter csvWriter, ReportGenerationDto record) {
        String[] data = {
            reportProperties.getTrCode(),
            record.getReportingDate(),
            reportProperties.getRegulationReference(),
            record.getReportName(),
            record.getReportType(),
            record.getReportGenerationTime(),
            record.getReportCompletionTime(),
            record.getReportPublicationTime(),
            record.getDate(),
            record.getSla(),
            record.getDifference(),
            TR_INCIDENT_ID
        };
        csvWriter.writeNext(data);
    }

    private String getFileName(String itemDate) {
        return ItemType.REPORT_GENERATION.getNamePattern() + itemDate + ".csv";
    }

}

