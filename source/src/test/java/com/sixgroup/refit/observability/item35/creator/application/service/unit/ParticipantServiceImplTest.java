package com.sixgroup.refit.observability.item35.creator.application.service.unit;

import com.sixgroup.refit.observability.item35.creator.application.service.ParticipantServiceImpl;
import com.sixgroup.refit.observability.item35.creator.configuration.ParticipantFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.domain.repository.ReportingFileRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.ParticipantDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.mappper.ParticipantMapper;
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

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.DATE_FORMAT_dd_MM_yyyy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParticipantServiceImplTest {

    @InjectMocks
    private ParticipantServiceImpl participantService;
    @Mock
    private ReportingFileRepository reportingFileRepository;
    @Mock
    private ParticipantMapper participantMapper;

    @Test
    void findParticipants_repository_return_empty_list() {

        doReturn(List.of()).when(reportingFileRepository)
            .findParticipantsByDayAccountAndFileType(anyString(), anyString());

        assertEquals(participantService.findParticipants("2024-02-01", "2024-03-01", "20240229"),
            Collections.emptyList());

        verify(reportingFileRepository, times(1)).findParticipantsByDayAccountAndFileType(anyString(), anyString());

    }

    @Test
    void findParticipants_ok() {
        ParticipantDTO participant_1 = new ParticipantDTO("TAR108", Timestamp.valueOf("2024-02-20 18:55:23.512"),
            Timestamp.valueOf("2024-02-20 18:55:23.512"), Timestamp.valueOf("2024-02-20 18:55:29.771"));

        ParticipantDTO participant_2 = new ParticipantDTO("TSR107", Timestamp.valueOf("2024-02-22 19:13:44.222"),
            Timestamp.valueOf("2024-02-22 19:13:44.222"), Timestamp.valueOf("2024-02-22 19:13:50.191"));

        doReturn(List.of(participant_1, participant_2)).when(reportingFileRepository)
            .findParticipantsByDayAccountAndFileType(anyString(), anyString());

        doReturn(getExpectedValue(participant_1)).when(participantMapper)
            .toReportGenerationDto(any(), any());

        List<ReportGenerationDto> participants = participantService
            .findParticipants("2024-02-01", "2024-03-01", "20240229");

        assertNotEquals(participants, Collections.emptyList());

        assertEquals(2, participants.size());

        verify(reportingFileRepository, times(1))
            .findParticipantsByDayAccountAndFileType(anyString(), anyString());

        verify(participantMapper,
            times(2)).toReportGenerationDto(any(), any());

    }

    private static ReportGenerationDto getExpectedValue(ParticipantDTO participant) {
        OffsetDateTime originDate = OffsetDateTime
            .of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        Duration diffInitEndDate = Duration.between(participant.getInitDate().toLocalDateTime(),
            participant.getEndDate().toLocalDateTime());

        String reportGenerationTimeString = originDate.plus(diffInitEndDate)
            .truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_INSTANT);

        String reportCompletionAndPubTime = participant.getEndDate().toLocalDateTime().truncatedTo(ChronoUnit.SECONDS)
            .atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);

        String date = participant.getReportingSession().toLocalDateTime()
            .format(DateTimeFormatter.ofPattern(DATE_FORMAT_dd_MM_yyyy));

        String sla = participant.getReportingSession().toLocalDateTime().truncatedTo(ChronoUnit.DAYS)
            .atOffset(ZoneOffset.UTC).plusDays(1).plusHours(6).format((DateTimeFormatter.ISO_INSTANT));
        return new ReportGenerationDto(
            "2024-02-29",
            "TAR108",
            "PARTICIPANT",
            reportGenerationTimeString,
            reportCompletionAndPubTime,
            reportCompletionAndPubTime,
            date,
            sla,
            calculateDifference(participant));
    }
    private static String calculateDifference(ParticipantDTO participant_1) {
        OffsetDateTime endDateOffsetDateTime = participant_1.getEndDate().toLocalDateTime().truncatedTo(ChronoUnit.SECONDS)
            .atOffset(ZoneOffset.UTC);
        OffsetDateTime slaDateOffsetDateTime = participant_1.getReportingSession().toLocalDateTime().truncatedTo(ChronoUnit.DAYS)
            .atOffset(ZoneOffset.UTC).plusDays(1).plusHours(6);

        if (endDateOffsetDateTime.isAfter(slaDateOffsetDateTime)) {
            // NEXT DAY FROM reportingSession
            slaDateOffsetDateTime = participant_1.getReportingSession().toLocalDateTime().atOffset(ZoneOffset.UTC).plusDays(1);
        }

        // REPORT_PUBLICATION_TIME (NOW IS USING endDate) - SLA
        // The format is a float that indicates the difference time between this two dates.
        float seconds = Duration.between(participant_1.getEndDate().toLocalDateTime().atOffset(ZoneOffset.UTC),
            slaDateOffsetDateTime).getSeconds() / 3600.00f;
        return new BigDecimal(seconds).setScale(1, RoundingMode.DOWN).toString();
    }

}
