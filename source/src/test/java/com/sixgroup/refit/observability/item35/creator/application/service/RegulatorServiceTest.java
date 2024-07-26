package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.configuration.RegulatorFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.domain.config.ReportConfig;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReguIdentityDTO;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.domain.repository.control.ReportingFileAdapterRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.RegulatorDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.repository.kudu.account.ReguIdentityAdapterRepository;
import com.sixgroup.refit.observability.item35.creator.shared.DataTestUtils;
import com.sixgroup.refit.observability.item35.creator.shared.sla.SlaInfoRepository;
import com.sixgroup.refit.observability.modules.validate.domain.data.SlaInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.REGULATOR_ENTITY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegulatorServiceTest {

    @InjectMocks
    private RegulatorService regulatorService;
    @Mock
    private ReportingFileAdapterRepository reportingFileAdapterRepository;
    @Mock
    private RegulatorFileTypeProperties fileTypeProperties;
    @Mock
    private SlaInfoRepository slaInfoRepository;

    @Mock
    private ReguIdentityAdapterRepository reguIdentityAdapterRepository;

    @Test
    void findRegulators_repository_return_empty_list() {
        ReflectionTestUtils.setField(regulatorService, "blockSize", 1);
        when(reportingFileAdapterRepository.findRegulatorByDayAccountAndFileType(anyString(), anyString())).thenReturn(new ArrayList<>());
        final List<ReportGenerationDto> response = regulatorService.findRegulator("2024-02-01", "2024-03-01", "20240215");
        assertTrue(response.isEmpty());
        verify(reportingFileAdapterRepository, times(1)).findRegulatorByDayAccountAndFileType(anyString(), anyString());

    }

    @Test
    void findRegulators_ok() {
        ReflectionTestUtils.setField(regulatorService, "blockSize", 1);
        final String fileName1 = "TRRGS_DATTSR_ESMAS_R15923-20240220_001001-0.zip";
        final String reportType1 = "TAR108";
        final LocalDateTime reportSessionDate1 = DataTestUtils.parseString("2024-02-20 18:55:23");
        final String accountId1 = "eudritrace";
        final LocalDateTime creationDate1 = DataTestUtils.parseString("2024-02-20 18:55:29");

        final String fileName2 = "TRRGS_DATMDA_EUDRIRA1051_R60004-20240222_001001-0.zip";
        final String reportType2 = "TSR107";
        final LocalDateTime reportSessionDate2 = DataTestUtils.parseString("2024-02-20 18:55:23");
        final String accountId2 = "eudritrace";
        final LocalDateTime creationDate2 = DataTestUtils.parseString("2024-02-20 18:55:29");

        final RegulatorDTO regulator1 = new RegulatorDTO(fileName1, reportType1, reportSessionDate1, accountId1, creationDate1, "CAESR");
        final RegulatorDTO regulator2 = new RegulatorDTO(fileName2, reportType2, reportSessionDate2, accountId2, creationDate2, "CAFAA");

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

        fileTypeProperties.setReportTypeEsma("ESMA");
        fileTypeProperties.setReportTypeNca("NCA");

        final ReportConfig reportConfig1 = new ReportConfig();
        reportConfig1.setName(reportType1);
        reportConfig1.setReportName(reportType1);
        final ReportConfig reportConfig2 = new ReportConfig();
        reportConfig2.setName(reportType2);
        reportConfig2.setReportName(reportType2);
        fileTypeProperties.getReports().add(reportConfig1);
        fileTypeProperties.getReports().add(reportConfig2);

        when(reportingFileAdapterRepository.findRegulatorByDayAccountAndFileType(anyString(), anyString())).thenReturn(List.of(regulator1, regulator2));
        when(slaInfoRepository.getSlaInfo(REGULATOR_ENTITY, reportType1, reportSessionDate1, creationDate1)).thenReturn(Optional.of(slaInfo1));
        when(slaInfoRepository.getSlaInfo(REGULATOR_ENTITY, reportType2, reportSessionDate2, creationDate2)).thenReturn(Optional.of(slaInfo2));
        when(reguIdentityAdapterRepository.findByTraceCode(any())).thenReturn(List.of(ReguIdentityDTO.builder().traceCode("CAESR").regulatorId("eudri2frb000").build()
            , ReguIdentityDTO.builder().traceCode("CAESR").regulatorId("eudri96jn000").build()));
        final List<ReportGenerationDto> response = regulatorService.findRegulator("2024-02-01", "2024-03-01", "20240215");

        assertFalse(response.isEmpty());
        assertEquals(2, response.size());

        verify(reportingFileAdapterRepository,
            times(1)).findRegulatorByDayAccountAndFileType(anyString(), anyString());
    }
}
