package com.sixgroup.refit.observability.item35.creator.infrastructure.mappper;

import com.sixgroup.refit.observability.item35.creator.configuration.ParticipantFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.ParticipantDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.DATE_FORMAT_dd_MM_yyyy;

@Mapper(componentModel = "spring")
@RequiredArgsConstructor
@Slf4j
public abstract class ParticipantMapper {
    private static final float TO_HOURS = 3600.00f;

    @BeforeMapping
    protected void manageData(ParticipantDTO participant, ParticipantFileTypeProperties fileTypeProperties,
                              @MappingTarget ReportGenerationDto reportGenerationDto) {

        reportGenerationDto.setReportName(getReportName(participant.getFileType(), fileTypeProperties));
        reportGenerationDto.setReportType(fileTypeProperties.getREPORT_TYPE());
        reportGenerationDto.setReportGenerationTime(calculateReportGenerationTime(participant));
        String reportCompletionTime = calculateReportCompletionTime(participant.getEndDate());
        reportGenerationDto.setReportCompletionTime(reportCompletionTime);
//          PUBLICATION TIME, PENDING NOW USE endDate
        reportGenerationDto.setReportPublicationTime(reportCompletionTime);
        reportGenerationDto.setDate(calculateDate(participant.getReportingSession()));
        reportGenerationDto.setSla(calculateSlaDate(participant.getReportingSession()));
//          DIFFERENCE -> REPORT_PUBLICATION_TIME - SLA (if endDate > slaDate -> SLA = NEXT DAY FROM reportingSession)
        BigDecimal bigDecimalDifference = calculateDifference(participant);
        reportGenerationDto.setDifference(bigDecimalDifference.intValue() == 0
            ? "" :
            bigDecimalDifference.toString());
    }

    public abstract ReportGenerationDto toReportGenerationDto(ParticipantDTO participant,
                                                              ParticipantFileTypeProperties fileTypeProperties);

    private String getReportName(String fileType, ParticipantFileTypeProperties fileTypeProperties) {
        String reportName;
        if (fileTypeProperties.getTYPES().containsValue(fileType)) {
            reportName = fileTypeProperties.getTYPES().get(fileType + ".REPORT_NAME");
        } else {
            log.error("'FileType: " + fileType + "' not exist in participant config map");
            throw new RuntimeException("'FileType: " + fileType + "' not exist in participant config map");
        }
        return reportName;
    }

    private static BigDecimal calculateDifference(ParticipantDTO participant) {
        OffsetDateTime endDateOffsetDateTime = participant.getEndDate().toLocalDateTime().truncatedTo(ChronoUnit.SECONDS)
            .atOffset(ZoneOffset.UTC);
        OffsetDateTime slaDateOffsetDateTime = getSlaDateOffsetDateTime(participant.getReportingSession());

        if (endDateOffsetDateTime.isAfter(slaDateOffsetDateTime)) {
            // NEXT DAY FROM reportingSession
            slaDateOffsetDateTime = participant.getReportingSession().toLocalDateTime().atOffset(ZoneOffset.UTC).plusDays(1);
            // REPORT_PUBLICATION_TIME (NOW IS USING endDate) - SLA
            // The format is a float that indicates the diference of time between this two dates.
            float seconds = Duration.between(participant.getEndDate().toLocalDateTime().atOffset(ZoneOffset.UTC),
                slaDateOffsetDateTime).getSeconds() / TO_HOURS;
            return new BigDecimal(seconds).setScale(1, RoundingMode.DOWN);
        }
        return new BigDecimal(0).setScale(1, RoundingMode.DOWN);
    }



    private static String calculateSlaDate(Timestamp reportingSession) {
        return getSlaDateOffsetDateTime(reportingSession).format(DateTimeFormatter.ISO_INSTANT);
    }

    private static OffsetDateTime getSlaDateOffsetDateTime(Timestamp timestamp) {
        return timestamp.toLocalDateTime().truncatedTo(ChronoUnit.DAYS)
            .atOffset(ZoneOffset.UTC).plusDays(1).plusHours(6);
    }

    private static String calculateDate(Timestamp reportingSession) {
        return reportingSession.toLocalDateTime().format(DateTimeFormatter.ofPattern(DATE_FORMAT_dd_MM_yyyy));
    }

    private static String calculateReportCompletionTime(Timestamp endDate) {
        return endDate.toLocalDateTime().truncatedTo(ChronoUnit.SECONDS)
            .atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
    }

    private static String calculateReportGenerationTime(ParticipantDTO participant) {
        OffsetDateTime originDate = OffsetDateTime
            .of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        Duration between = Duration.between(participant.getInitDate().toLocalDateTime(),
            participant.getEndDate().toLocalDateTime()).truncatedTo(ChronoUnit.SECONDS);
        return originDate.plus(between).format(DateTimeFormatter.ISO_INSTANT);
    }
}
