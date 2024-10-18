package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.configuration.ParticipantFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.domain.config.ReportConfig;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.domain.repository.control.ReportingFileAdapterRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.ParticipantDTO;
import com.sixgroup.refit.observability.item35.creator.shared.DataTestUtils;
import com.sixgroup.refit.observability.item35.creator.shared.sla.SlaInfoRepository;
import com.sixgroup.refit.observability.modules.validate.domain.data.SlaInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.AppConstants.PARTICIPANT_ENTITY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipantServiceTest {

    @InjectMocks
    private ParticipantService participantService;
    @Mock
    private ReportingFileAdapterRepository reportingFileAdapterRepository;
    @Mock
    private SlaInfoRepository slaInfoRepository;
    @Mock
    private ParticipantFileTypeProperties fileTypeProperties;

    @Test
    void findParticipants_repository_return_empty_list() {
        when(reportingFileAdapterRepository.findParticipantsByDayAccountAndFileType(anyString(), anyString())).thenReturn(new ArrayList<>());
        when(reportingFileAdapterRepository.findParticipantsRecoFileType(anyString(), anyString())).thenReturn(new ArrayList<>());

        final List<ReportGenerationDto> response = participantService.findParticipants("2024-02-01", "2024-03-01", "20240215");

        assertTrue(response.isEmpty());
        verify(reportingFileAdapterRepository, times(1)).findParticipantsByDayAccountAndFileType(anyString(), anyString());
        verify(reportingFileAdapterRepository, times(1)).findParticipantsRecoFileType(anyString(), anyString());

    }

    @Test
    void findParticipants_ok() {
        final String reportType1 = "TAR108";
        final LocalDateTime reportSessionDate1 = DataTestUtils.parseString("2024-02-20 18:55:23");
        final LocalDateTime initReportDate1 = DataTestUtils.parseString("2024-02-20 18:25:29");
        final LocalDateTime endReportDate1 = DataTestUtils.parseString("2024-02-20 18:55:29");

        final String reportType2 = "TSR107";
        final LocalDateTime reportSessionDate2 = DataTestUtils.parseString("2024-02-20 18:55:23");
        final LocalDateTime initReportDate2 = DataTestUtils.parseString("2024-02-20 18:55:23");
        final LocalDateTime endReportDate2 = DataTestUtils.parseString("2024-02-20 18:55:29");

        final String reportTypeReco1 = "REC091";
        final LocalDateTime reportSessionDateReco1 = DataTestUtils.parseString("2024-02-20 18:55:23");
        final LocalDateTime initReportDateReco1 = DataTestUtils.parseString("2024-02-20 18:55:23");
        final LocalDateTime endReportDateReco1 = DataTestUtils.parseString("2024-02-20 18:55:29");

        final ParticipantDTO participant1 = new ParticipantDTO(reportType1, reportSessionDate1, initReportDate1, endReportDate1);
        final ParticipantDTO participant2 = new ParticipantDTO(reportType2, reportSessionDate2, initReportDate2, endReportDate2);
        final ParticipantDTO participantReco1 = new ParticipantDTO(reportTypeReco1, reportSessionDateReco1, initReportDateReco1, endReportDateReco1);

        final SlaInfo slaInfo1 = SlaInfo.builder()
            .meetsSla(Boolean.TRUE)
            .expectSlaDate(reportSessionDate1.plusDays(1).truncatedTo(ChronoUnit.DAYS).plusHours(6))
            .generationDuration(Duration.ofMinutes(30))
            .build();

        final SlaInfo slaInfo2 = SlaInfo.builder()
            .meetsSla(Boolean.TRUE)
            .expectSlaDate(reportSessionDate1.plusDays(1).truncatedTo(ChronoUnit.DAYS).plusHours(12))
            .generationDuration(Duration.ofMinutes(60))
            .build();

        fileTypeProperties.setReportType("PARTICIPANT");

        final ReportConfig reportConfig1 = new ReportConfig();
        reportConfig1.setName(reportType1);
        reportConfig1.setReportName(reportType1);
        final ReportConfig reportConfig2 = new ReportConfig();
        reportConfig2.setName(reportType2);
        reportConfig2.setReportName(reportType2);
        fileTypeProperties.getReports().add(reportConfig1);
        fileTypeProperties.getReports().add(reportConfig2);

        when(reportingFileAdapterRepository.findParticipantsByDayAccountAndFileType(anyString(), anyString())).thenReturn(List.of(participant1, participant2));
        when(reportingFileAdapterRepository.findParticipantsRecoFileType(anyString(), anyString())).thenReturn(List.of(participantReco1));
        when(slaInfoRepository.getSlaInfo(PARTICIPANT_ENTITY, reportType1, reportSessionDate1, initReportDate1, endReportDate1)).thenReturn(Optional.of(slaInfo1));
        when(slaInfoRepository.getSlaInfo(PARTICIPANT_ENTITY, reportType2, reportSessionDate2, initReportDate2, endReportDate2)).thenReturn(Optional.of(slaInfo2));

        final List<ReportGenerationDto> response = participantService.findParticipants("2024-02-01", "2024-03-01", "20240215");

        assertFalse(response.isEmpty());
        assertEquals(2, response.size());

        verify(reportingFileAdapterRepository, times(1))
            .findParticipantsByDayAccountAndFileType(anyString(), anyString());
        verify(reportingFileAdapterRepository, times(1))
            .findParticipantsRecoFileType(anyString(), anyString());

    }

}
