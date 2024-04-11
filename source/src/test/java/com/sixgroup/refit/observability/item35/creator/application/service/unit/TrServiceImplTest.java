package com.sixgroup.refit.observability.item35.creator.application.service.unit;

import com.sixgroup.refit.observability.item35.creator.application.service.TrServiceImpl;
import com.sixgroup.refit.observability.item35.creator.configuration.TrFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.domain.repository.ReportingFileRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.ParticipantDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.RegulatorDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.TrDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.mappper.TrMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class TrServiceImplTest {

    @InjectMocks
    private TrServiceImpl trService;
    @Mock
    private ReportingFileRepository reportingFileRepository;
    @Mock
    private TrMapper trMapper;

    @Test
    void findTrs_repository_return_empty_list() {

        doReturn(List.of()).when(reportingFileRepository)
            .findTrByDayAccountAndFileType(anyString(), anyString());

        assertEquals(trService.findTr("2024-02-01", "2024-03-01", "20240229"),
            Collections.emptyList());

        verify(reportingFileRepository, times(1)).findTrByDayAccountAndFileType(anyString(), anyString());

    }

    @Test
    void findTr_ok() {
        TrDTO tr_1 = new TrDTO("TD", Timestamp.valueOf("2024-02-22 14:08:12.550"),
            "trkdp", Timestamp.valueOf("2024-02-22 14:08:12.550"));

        TrDTO tr_2 = new TrDTO("RL", Timestamp.valueOf("2024-02-23 07:34:59.289"),
            "trdti", Timestamp.valueOf("2024-02-23 07:34:59.289"));

        doReturn(List.of(tr_1, tr_2)).when(reportingFileRepository)
            .findTrByDayAccountAndFileType(anyString(), anyString());

        doReturn(getExpectedValue(tr_1)).when(trMapper).toReportGenerationDto(any(), any());

        List<ReportGenerationDto> trs = trService
            .findTr("2024-02-01", "2024-03-01", "20240229");

        assertNotEquals(trs, Collections.emptyList());

        assertEquals(2, trs.size());

        verify(reportingFileRepository, times(1))
            .findTrByDayAccountAndFileType(anyString(), anyString());

        verify(trMapper, times(2)).toReportGenerationDto(any(), any());

    }

    private static ReportGenerationDto getExpectedValue(TrDTO tr_1) {
        // VALIDATION DATA

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

        return new ReportGenerationDto(
            "2024-02-29",
            "trkdp-TD",
            "TR",
            reportGenerationTimeString,
            reportCompletionAndPubTime,
            reportCompletionAndPubTime,
            date,
            sla,
            calculateDifference(tr_1));
    }

    private static String calculateDifference(TrDTO tr) {
        OffsetDateTime creationDateOffsetDateTime = tr.getCreationDate().toLocalDateTime()
            .truncatedTo(ChronoUnit.SECONDS).atOffset(ZoneOffset.UTC);
        OffsetDateTime slaDateOffsetDateTime = tr.getReportingSession().toLocalDateTime().truncatedTo(ChronoUnit.DAYS)
            .atOffset(ZoneOffset.UTC).plusDays(1).plusHours(12);

        if (creationDateOffsetDateTime.isAfter(slaDateOffsetDateTime)) {
            // NEXT DAY FROM reportingSession
            slaDateOffsetDateTime = tr.getReportingSession().toLocalDateTime().atOffset(ZoneOffset.UTC).plusDays(1);
        }

        // REPORT_PUBLICATION_TIME (NOW IS USING creationDate) - SLA
        // The format is a float that indicates the diference of time between this two dates.
        float seconds = Duration.between(tr.getCreationDate().toLocalDateTime().atOffset(ZoneOffset.UTC),
            slaDateOffsetDateTime).getSeconds() / 3600.00f;
        return new BigDecimal(seconds).setScale(1, RoundingMode.DOWN).toString();
    }

}
