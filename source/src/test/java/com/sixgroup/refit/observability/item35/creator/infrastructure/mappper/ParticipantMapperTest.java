package com.sixgroup.refit.observability.item35.creator.infrastructure.mappper;

import com.sixgroup.refit.observability.item35.creator.configuration.ParticipantFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.domain.config.ReportConfig;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.ParticipantDTO;
import com.sixgroup.refit.observability.item35.creator.shared.DataTestUtils;
import com.sixgroup.refit.observability.modules.validate.domain.data.SlaInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static com.sixgroup.refit.observability.item35.creator.shared.DataTestUtils.ORIGIN_DATE;
import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.DATE_FORMAT_YYYY_MM_DD;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ParticipantMapperTest {

    @Test
    void mapper_participants_ok() {
        final String reportType = "TAR108";
        final LocalDateTime reportSessionDate = DataTestUtils.parseString("2024-02-20 18:55:23");
        final LocalDateTime initReportDate = DataTestUtils.parseString("2024-02-20 18:25:29");
        final LocalDateTime endReportDate = DataTestUtils.parseString("2024-02-20 18:55:29");

        final ParticipantFileTypeProperties fileTypeProperties = new ParticipantFileTypeProperties();
        fileTypeProperties.setReportType("participant");
        final ReportConfig reportConfig = new ReportConfig();
        reportConfig.setName(reportType);
        reportConfig.setReportName(reportType);
        fileTypeProperties.getReports().add(reportConfig);

        final ParticipantDTO participantDTO = new ParticipantDTO(reportType, reportSessionDate, initReportDate, endReportDate);

        final SlaInfo slaInfo = SlaInfo.builder()
            .meetsSla(Boolean.TRUE)
            .expectSlaDate(reportSessionDate.plusDays(1).truncatedTo(ChronoUnit.DAYS).plusHours(6))
            .generationDuration(Duration.ofMinutes(30))
            .build();

        final ParticipantMapper participantMapper = new ParticipantMapper();
        final ReportGenerationDto response = participantMapper.toReportGenerationDto(participantDTO, fileTypeProperties, slaInfo);

        Duration diffInitEndDate = Duration.between(participantDTO.getInitDate(), participantDTO.getEndDate());

        String reportGenerationTimeString = ORIGIN_DATE.plus(diffInitEndDate)
            .truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_INSTANT);

        String reportCompletionAndPubTime = participantDTO.getEndDate().truncatedTo(ChronoUnit.SECONDS)
            .atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);

        String date = participantDTO.getReportingSession()
            .format(DateTimeFormatter.ofPattern(DATE_FORMAT_YYYY_MM_DD));

        String sla = participantDTO.getReportingSession().truncatedTo(ChronoUnit.DAYS)
            .atOffset(ZoneOffset.UTC).plusDays(1).plusHours(6).format((DateTimeFormatter.ISO_INSTANT));

        ReportGenerationDto expectedValue = new ReportGenerationDto(
            null,
            "TAR108",
            "participant",
            reportGenerationTimeString,
            reportCompletionAndPubTime,
            null,
            date,
            sla, null);

        assertEquals(expectedValue, response);

    }

}
