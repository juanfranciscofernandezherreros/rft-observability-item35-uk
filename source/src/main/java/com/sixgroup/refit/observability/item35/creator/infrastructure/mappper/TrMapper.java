package com.sixgroup.refit.observability.item35.creator.infrastructure.mappper;

import com.sixgroup.refit.observability.item35.creator.configuration.RegulatorFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.TrFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.RegulatorDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.TrDTO;
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
public abstract class TrMapper {
    private static final float TO_HOURS = 3600.00f;

    @BeforeMapping
    protected void manageData(TrDTO tr, TrFileTypeProperties fileTypeProperties,
                              @MappingTarget ReportGenerationDto reportGenerationDto) {
        reportGenerationDto.setReportName(getReportName(tr.getAccountId(), tr.getFileType(), fileTypeProperties));
        reportGenerationDto.setReportType(fileTypeProperties.getREPORT_TYPE());
        reportGenerationDto.setReportGenerationTime(calculateReportGenerationTime(tr));
        String reportCompletionTime = calculateReportCompletionTime(tr.getCreationDate());
        reportGenerationDto.setReportCompletionTime(reportCompletionTime);
//          PUBLICATION TIME, PENDING NOW USE creationDate
        reportGenerationDto.setReportPublicationTime(reportCompletionTime);
        reportGenerationDto.setDate(calculateDate(tr.getReportingSession()));
        reportGenerationDto.setSla(calculateSlaDate(tr.getReportingSession()));
//          DIFFERENCE -> REPORT_PUBLICATION_TIME - SLA
        reportGenerationDto.setDifference(calculateDifference(tr).toString());
    }

    public abstract ReportGenerationDto toReportGenerationDto(TrDTO regulator,
                                                              TrFileTypeProperties fileTypeProperties);

    private static BigDecimal calculateDifference(TrDTO tr) {
        OffsetDateTime creationDateOffsetDateTime = tr.getCreationDate().toLocalDateTime()
            .truncatedTo(ChronoUnit.SECONDS).atOffset(ZoneOffset.UTC);
        OffsetDateTime slaDateOffsetDateTime = getSlaDateOffsetDateTime(tr.getReportingSession());

        if (creationDateOffsetDateTime.isAfter(slaDateOffsetDateTime)) {
            // NEXT DAY FROM reportingSession
            slaDateOffsetDateTime = tr.getReportingSession().toLocalDateTime().atOffset(ZoneOffset.UTC).plusDays(1);
        }

        // REPORT_PUBLICATION_TIME (NOW IS USING creationDate) - SLA
        // The format is a float that indicates the diference of time between this two dates.
        float seconds = Duration.between(tr.getCreationDate().toLocalDateTime().atOffset(ZoneOffset.UTC),
            slaDateOffsetDateTime).getSeconds() / TO_HOURS;
        return new BigDecimal(seconds).setScale(1, RoundingMode.DOWN);
    }

    private String getReportName(String accountId, String fileType, TrFileTypeProperties fileTypeProperties) {
        if (!fileTypeProperties.getTYPES().containsValue(fileType)) {
            log.error("'FileType: " + fileType + "' not exist in tr config map");
            throw new RuntimeException("'FileType: " + fileType + "' not exist in tr config map");
        }
        return accountId + "-" + fileTypeProperties.getTYPES().get(fileType + ".REPORT_NAME");
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

    private static String calculateReportGenerationTime(TrDTO tr) {
        OffsetDateTime originDate = OffsetDateTime
            .of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        LocalDateTime localDateTimeCreationDate = tr.getCreationDate().toLocalDateTime();

        return originDate
            .withHour(localDateTimeCreationDate.getHour())
            .withMinute(localDateTimeCreationDate.getMinute())
            .withSecond(localDateTimeCreationDate.getSecond()).format(DateTimeFormatter.ISO_INSTANT);
    }
}
