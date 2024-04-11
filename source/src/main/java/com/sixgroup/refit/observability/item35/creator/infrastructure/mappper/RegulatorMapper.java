package com.sixgroup.refit.observability.item35.creator.infrastructure.mappper;

import com.sixgroup.refit.observability.item35.creator.configuration.RegulatorFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.RegulatorDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.DATE_FORMAT_dd_MM_yyyy;

@Mapper(componentModel = "spring")
@RequiredArgsConstructor
@Slf4j
public abstract class RegulatorMapper {
    private static final float TO_HOURS = 3600.00f;

    @BeforeMapping
    protected void manageData(RegulatorDTO regulator, RegulatorFileTypeProperties fileTypeProperties,
                              @MappingTarget ReportGenerationDto reportGenerationDto) {

        reportGenerationDto.setReportName(getReportName(regulator.getFileName(), regulator.getFileType(), fileTypeProperties));
        reportGenerationDto.setReportType(regulator.getFileName().contains("ESMA")
            ? fileTypeProperties.getREPORT_TYPE_ESMA()
            : fileTypeProperties.getREPORT_TYPE_NCA());
        reportGenerationDto.setReportGenerationTime(calculateReportGenerationTime(regulator));
        String reportCompletionTime = calculateReportCompletionTime(regulator.getCreationDate());
        reportGenerationDto.setReportCompletionTime(reportCompletionTime);
//          PUBLICATION TIME, PENDING, NOW USE creationDate
        reportGenerationDto.setReportPublicationTime(reportCompletionTime);
        reportGenerationDto.setDate(calculateDate(regulator.getReportingSession()));
        reportGenerationDto.setSla(calculateSlaDate(regulator.getReportingSession()));
//          DIFFERENCE -> REPORT_PUBLICATION_TIME - SLA
        reportGenerationDto.setDifference(calculateDifference(regulator).toString());

    }

    public abstract ReportGenerationDto toReportGenerationDto(RegulatorDTO regulator,
                                                              RegulatorFileTypeProperties fileTypeProperties);

    private static BigDecimal calculateDifference(RegulatorDTO regulatorDTO) {
        OffsetDateTime creationDateOffsetDateTime = regulatorDTO.getCreationDate().toLocalDateTime()
            .truncatedTo(ChronoUnit.SECONDS).atOffset(ZoneOffset.UTC);
        OffsetDateTime slaDateOffsetDateTime = getSlaDateOffsetDateTime(regulatorDTO.getReportingSession());

        if (creationDateOffsetDateTime.isAfter(slaDateOffsetDateTime)) {
            // NEXT DAY FROM reportingSession
            slaDateOffsetDateTime = regulatorDTO.getReportingSession().toLocalDateTime().atOffset(ZoneOffset.UTC).plusDays(1);
        }

        // REPORT_PUBLICATION_TIME (NOW IS USING creationDate) - SLA
        // The format is a float that indicates the diference of time between this two dates.
        float seconds = Duration.between(regulatorDTO.getCreationDate().toLocalDateTime().atOffset(ZoneOffset.UTC),
            slaDateOffsetDateTime).getSeconds() / TO_HOURS;
        return new BigDecimal(seconds).setScale(1, RoundingMode.DOWN);
    }

    private String getReportName(String fileName, String fileType, RegulatorFileTypeProperties fileTypeProperties) {
        String[] splitBy_char = fileName.split("_");
        if (splitBy_char.length <= 2) {
            log.error("'FileName: " + fileName + "' not contain expected position");
            throw new RuntimeException("'FileName: " + fileName + "' not contain expected position");
        }
        String fileNameValue = splitBy_char[2];
        if (!fileTypeProperties.getTYPES().containsValue(fileType)) {
            log.error("'FileType: " + fileType + "' not exist in regulator config map");
            throw new RuntimeException("'FileType: " + fileType + "' not exist in regulator config map");
        }
        return fileNameValue + "-" + fileTypeProperties.getTYPES().get(fileType + ".REPORT_NAME");
    }

    private static String calculateSlaDate(Timestamp reportingSession) {
        return getSlaDateOffsetDateTime(reportingSession).format(DateTimeFormatter.ISO_INSTANT);
    }

    private static OffsetDateTime getSlaDateOffsetDateTime(Timestamp timestamp) {
        return timestamp.toLocalDateTime().truncatedTo(ChronoUnit.DAYS)
            .atOffset(ZoneOffset.UTC).plusDays(1).plusHours(12);
    }

    private static String calculateDate(Timestamp reportingSession) {
        return reportingSession.toLocalDateTime().format(DateTimeFormatter.ofPattern(DATE_FORMAT_dd_MM_yyyy));
    }

    private static String calculateReportCompletionTime(Timestamp creationDate) {
        return creationDate.toLocalDateTime().truncatedTo(ChronoUnit.SECONDS)
            .atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
    }

    private static String calculateReportGenerationTime(RegulatorDTO regulatorDTO) {
        OffsetDateTime originDate = OffsetDateTime
            .of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        LocalDateTime localDateTimeCreationDate = regulatorDTO.getCreationDate().toLocalDateTime();

        return originDate
            .withHour(localDateTimeCreationDate.getHour())
            .withMinute(localDateTimeCreationDate.getMinute())
            .withSecond(localDateTimeCreationDate.getSecond()).format(DateTimeFormatter.ISO_INSTANT);
    }
}
