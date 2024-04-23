package com.sixgroup.refit.observability.item35.creator.infrastructure.mappper;

import com.sixgroup.refit.observability.item35.creator.configuration.TrFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.TrDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.DATE_FORMAT_dd_MM_yyyy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrMapperTest {

    @Mock
    private TrFileTypeProperties fileTypeProperties;

    @Test
    void findTr_ok() {
        TrDTO tr_1 = new TrDTO("TD", Timestamp.valueOf("2024-02-22 14:08:12.550"),
            "trkdp", Timestamp.valueOf("2024-02-22 14:08:12.550"));

        when(fileTypeProperties.getREPORT_TYPE()).thenReturn("TR");
        when(fileTypeProperties.getTYPES())
            .thenReturn(Map.of("RL.REPORT_NAME", "RL",
                "RL.SLA", "yyyy-MM-ddT12:00:00Z",
                "RL.INIT", "yyyy-MM-ddT00:00:00Z",
                "TD.REPORT_NAME", "TD",
                "TD.SLA", "yyyy-MM-ddT06:00:00Z",
                "TD.INIT", "yyyy-MM-ddT00:00:00Z"));

        TrMapper trMapper = mock(TrMapper.class, Mockito.CALLS_REAL_METHODS);

        ReportGenerationDto result = new ReportGenerationDto();
        trMapper.manageData(tr_1, fileTypeProperties, result);

        OffsetDateTime originDate = OffsetDateTime
            .of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        OffsetDateTime offsetDateTimeCreationDate = tr_1.getCreationDate().toLocalDateTime().atOffset(ZoneOffset.UTC);

        String reportGenerationTimeString = originDate
            .withHour(offsetDateTimeCreationDate.getHour())
            .withMinute(offsetDateTimeCreationDate.getMinute())
            .withSecond(offsetDateTimeCreationDate.getSecond()).format(DateTimeFormatter.ISO_INSTANT);


        String reportCompletionAndPubTime = tr_1.getCreationDate().toLocalDateTime().truncatedTo(ChronoUnit.SECONDS)
            .atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);

        String date = tr_1.getReportingSession().toLocalDateTime()
            .format(DateTimeFormatter.ofPattern(DATE_FORMAT_dd_MM_yyyy));

        String sla = tr_1.getReportingSession().toLocalDateTime().truncatedTo(ChronoUnit.DAYS)
            .atOffset(ZoneOffset.UTC).plusDays(1).plusHours(12).format((DateTimeFormatter.ISO_INSTANT));

        ReportGenerationDto expectedValue = new ReportGenerationDto(
            null,
            "trkdp-TD",
            "TR",
            reportGenerationTimeString,
            reportCompletionAndPubTime,
            reportCompletionAndPubTime,
            date,
            sla,
            calculateDifference(tr_1));

        assertEquals(expectedValue, result);

        verify(trMapper, times(1)).manageData(any(), any(), any());

    }

    private static String calculateDifference(TrDTO tr) {
        OffsetDateTime creationDateOffsetDateTime = tr.getCreationDate().toLocalDateTime()
            .truncatedTo(ChronoUnit.SECONDS).atOffset(ZoneOffset.UTC);
        OffsetDateTime slaDateOffsetDateTime = tr.getReportingSession().toLocalDateTime().truncatedTo(ChronoUnit.DAYS)
            .atOffset(ZoneOffset.UTC).plusDays(1).plusHours(12);

        if (creationDateOffsetDateTime.isAfter(slaDateOffsetDateTime)) {
            // NEXT DAY FROM reportingSession
            slaDateOffsetDateTime = tr.getReportingSession().toLocalDateTime().atOffset(ZoneOffset.UTC).plusDays(1);
            // REPORT_PUBLICATION_TIME (NOW IS USING creationDate) - SLA
            // The format is a float that indicates the diference of time between this two dates.
            float seconds = Duration.between(tr.getCreationDate().toLocalDateTime().atOffset(ZoneOffset.UTC),
                slaDateOffsetDateTime).getSeconds() / 3600.00f;
            return new BigDecimal(seconds).setScale(1, RoundingMode.DOWN).toString();
        }
        return "";
    }

}
