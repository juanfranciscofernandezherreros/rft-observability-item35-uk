package com.sixgroup.refit.observability.item35.creator.infrastructure.mappper;

import com.sixgroup.refit.observability.item35.creator.configuration.ParticipantFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.ParticipantDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
import java.util.List;
import java.util.Map;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.DATE_FORMAT_dd_MM_yyyy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipantMapperTest {

    @Mock
    private ParticipantFileTypeProperties fileTypeProperties;

    @Test
    void mapper_participants_ok() {
        ParticipantDTO participant_1 = new ParticipantDTO("TAR108", Timestamp.valueOf("2024-02-20 18:55:23.512"),
            Timestamp.valueOf("2024-02-20 18:55:23.512"), Timestamp.valueOf("2024-02-20 18:55:29.771"));

        when(fileTypeProperties.getREPORT_TYPE()).thenReturn("PARTICIPANT");
        when(fileTypeProperties.getTYPES())
            .thenReturn(Map.of("TSR107.REPORT_NAME", "TSR107",
                "TSR107.SLA", "yyyy-MM-ddT06:00:00Z",
                "TAR108.REPORT_NAME", "TAR108",
                "TAR108.SLA", "yyyy-MM-ddT06:00:00Z"));

        ParticipantMapper participantMapper = mock(ParticipantMapper.class, Mockito.CALLS_REAL_METHODS);

        ReportGenerationDto result = new ReportGenerationDto();
        participantMapper.manageData(participant_1, fileTypeProperties, result);

        OffsetDateTime originDate = OffsetDateTime
            .of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        Duration diffInitEndDate = Duration.between(participant_1.getInitDate().toLocalDateTime(),
            participant_1.getEndDate().toLocalDateTime());

        String reportGenerationTimeString = originDate.plus(diffInitEndDate)
            .truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_INSTANT);

        String reportCompletionAndPubTime = participant_1.getEndDate().toLocalDateTime().truncatedTo(ChronoUnit.SECONDS)
            .atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);

        String date = participant_1.getReportingSession().toLocalDateTime()
            .format(DateTimeFormatter.ofPattern(DATE_FORMAT_dd_MM_yyyy));

        String sla = participant_1.getReportingSession().toLocalDateTime().truncatedTo(ChronoUnit.DAYS)
            .atOffset(ZoneOffset.UTC).plusDays(1).plusHours(6).format((DateTimeFormatter.ISO_INSTANT));

        ReportGenerationDto expectedValue = new ReportGenerationDto(
            null,
            "TAR108",
            "PARTICIPANT",
            reportGenerationTimeString,
            reportCompletionAndPubTime,
            reportCompletionAndPubTime,
            date,
            sla,
            calculateDifference(participant_1));

        assertEquals(expectedValue, result);

    }

    private static String calculateDifference(ParticipantDTO participant_1) {
        OffsetDateTime endDateOffsetDateTime = participant_1.getEndDate().toLocalDateTime().truncatedTo(ChronoUnit.SECONDS)
            .atOffset(ZoneOffset.UTC);
        OffsetDateTime slaDateOffsetDateTime = participant_1.getReportingSession().toLocalDateTime().truncatedTo(ChronoUnit.DAYS)
            .atOffset(ZoneOffset.UTC).plusDays(1).plusHours(6);

        if (endDateOffsetDateTime.isAfter(slaDateOffsetDateTime)) {
            // NEXT DAY FROM reportingSession
            slaDateOffsetDateTime = participant_1.getReportingSession().toLocalDateTime().atOffset(ZoneOffset.UTC).plusDays(1);
            // REPORT_PUBLICATION_TIME (NOW IS USING endDate) - SLA
            // The format is a float that indicates the difference time between this two dates.
            float seconds = Duration.between(participant_1.getEndDate().toLocalDateTime().atOffset(ZoneOffset.UTC),
                slaDateOffsetDateTime).getSeconds() / 3600.00f;
            return new BigDecimal(seconds).setScale(1, RoundingMode.DOWN).toString();
        }
        return "";
    }

}
