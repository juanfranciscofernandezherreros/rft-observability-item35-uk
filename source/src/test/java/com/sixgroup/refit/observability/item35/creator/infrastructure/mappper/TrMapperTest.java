package com.sixgroup.refit.observability.item35.creator.infrastructure.mappper;

import com.sixgroup.refit.observability.item35.creator.configuration.TrFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.domain.config.ReportConfig;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.TrDTO;
import com.sixgroup.refit.observability.item35.creator.shared.DataTestUtils;
import com.sixgroup.refit.observability.modules.validate.domain.data.SlaInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.DATE_FORMAT_DD_MM_YYYY;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class TrMapperTest {

    @Test
    void findTr_ok() {
        final String reportType = "TD107";
        final LocalDateTime reportSessionDate = DataTestUtils.parseString("2024-02-22 14:08:12");
        final String accountId = "trkdp";
        final LocalDateTime creationDate = DataTestUtils.parseString("2024-02-22 14:08:55");

        final TrFileTypeProperties fileTypeProperties = new TrFileTypeProperties();
        fileTypeProperties.setReportType("TR");
        final ReportConfig reportConfig = new ReportConfig();
        reportConfig.setName(reportType);
        reportConfig.setReportName(reportType);
        fileTypeProperties.getReports().add(reportConfig);

        final TrDTO trDTO1 = new TrDTO(reportType, reportSessionDate, accountId, creationDate);

        final SlaInfo slaInfo = SlaInfo.builder()
            .meetsSla(Boolean.TRUE)
            .expectSlaDate(reportSessionDate.plusDays(1).truncatedTo(ChronoUnit.DAYS).plusHours(12))
            .generationDuration(Duration.ofMinutes(14 * 60 + 8).plusSeconds(55))
            .build();

        final TrMapper trMapper = new TrMapper();
        final ReportGenerationDto response = trMapper.toReportGenerationDto(trDTO1, fileTypeProperties, slaInfo);

        OffsetDateTime originDate = OffsetDateTime
            .of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        OffsetDateTime offsetDateTimeCreationDate = trDTO1.getCreationDate().atOffset(ZoneOffset.UTC);

        String reportGenerationTimeString = originDate
            .withHour(offsetDateTimeCreationDate.getHour())
            .withMinute(offsetDateTimeCreationDate.getMinute())
            .withSecond(offsetDateTimeCreationDate.getSecond()).format(DateTimeFormatter.ISO_INSTANT);


        String reportCompletionAndPubTime = trDTO1.getCreationDate().truncatedTo(ChronoUnit.SECONDS)
            .atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);

        String date = trDTO1.getReportingSession()
            .format(DateTimeFormatter.ofPattern(DATE_FORMAT_DD_MM_YYYY));

        String sla = trDTO1.getReportingSession().truncatedTo(ChronoUnit.DAYS)
            .atOffset(ZoneOffset.UTC).plusDays(1).plusHours(12).format((DateTimeFormatter.ISO_INSTANT));

        ReportGenerationDto expectedValue = new ReportGenerationDto(
            null,
            "trkdp-TD107",
            "TR",
            reportGenerationTimeString,
            reportCompletionAndPubTime,
            null,
            date,
            sla, null);

        assertEquals(expectedValue, response);
    }
}
