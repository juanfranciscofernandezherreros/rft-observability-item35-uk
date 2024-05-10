package com.sixgroup.refit.observability.item35.creator.application.service.unit;

import com.sixgroup.refit.observability.item35.creator.application.service.TrService;
import com.sixgroup.refit.observability.item35.creator.configuration.TrFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.domain.config.ReportConfig;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.domain.repository.ReportingFileAdapterRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.TrDTO;
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

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.TR_ENTITY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrServiceTest {

    @InjectMocks
    private TrService trService;
    @Mock
    private ReportingFileAdapterRepository reportingFileAdapterRepository;
    @Mock
    private TrFileTypeProperties fileTypeProperties;
    @Mock
    private SlaInfoRepository slaInfoRepository;

    @Test
    void findTrs_repository_return_empty_list() {
        when(reportingFileAdapterRepository.findTrByDayAccountAndFileType(anyString(), anyString())).thenReturn(new ArrayList<>());

        final List<ReportGenerationDto> response = trService.findTr("2024-02-01", "2024-03-01", "20240229");

        assertTrue(response.isEmpty());
        verify(reportingFileAdapterRepository, times(1)).findTrByDayAccountAndFileType(anyString(), anyString());
    }

    @Test
    void findTr_ok() {
        final String reportType1 = "TD107";
        final LocalDateTime reportSessionDate1 = DataTestUtils.parseString("2024-02-22 14:08:55");
        final String accountId1 = "trkdp";
        final LocalDateTime creationDate1 = DataTestUtils.parseString("2024-02-22 14:08:12");

        final String reportType2 = "RL078";
        final LocalDateTime reportSessionDate2 = DataTestUtils.parseString("2024-02-23 07:34:59");
        final String accountId2 = "trdti";
        final LocalDateTime creationDate2 = DataTestUtils.parseString("2024-02-23 07:34:59");

        final TrDTO trDTO1 = new TrDTO(reportType1, reportSessionDate1, accountId1, creationDate1);
        final TrDTO trDTO2 = new TrDTO(reportType2, reportSessionDate2, accountId2, creationDate2);

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

        fileTypeProperties.setReportType("TR");
        final ReportConfig reportConfig1 = new ReportConfig();
        reportConfig1.setName(reportType1);
        reportConfig1.setReportName(reportType1);
        final ReportConfig reportConfig2 = new ReportConfig();
        reportConfig2.setName(reportType2);
        reportConfig2.setReportName(reportType2);
        fileTypeProperties.getReports().add(reportConfig1);
        fileTypeProperties.getReports().add(reportConfig2);

        when(reportingFileAdapterRepository.findTrByDayAccountAndFileType(anyString(), anyString())).thenReturn(List.of(trDTO1, trDTO2));
        when(slaInfoRepository.getSlaInfo(TR_ENTITY, reportType1, reportSessionDate1, creationDate1)).thenReturn(Optional.of(slaInfo1));
        when(slaInfoRepository.getSlaInfo(TR_ENTITY, reportType2, reportSessionDate2, creationDate2)).thenReturn(Optional.of(slaInfo2));

        final List<ReportGenerationDto> response = trService.findTr("2024-02-01", "2024-03-01", "20240229");

        assertFalse(response.isEmpty());
        assertEquals(2, response.size());

        verify(reportingFileAdapterRepository, times(1))
            .findTrByDayAccountAndFileType(anyString(), anyString());
    }
}
