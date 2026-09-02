package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.configuration.RegulatorProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.ReportItemProperties;
import com.sixgroup.refit.observability.item35.creator.domain.config.ReportConfig;
import com.sixgroup.refit.observability.item35.creator.domain.config.TranslationData;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReguIdentityDTO;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.domain.repository.control.ReportingFileAdapterRepository;
import com.sixgroup.refit.observability.item35.creator.domain.repository.reportstate.ReportEodProcessStateRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.RegulatorDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.sqlserver.ReportEoDDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.repository.kudu.account.ReguIdentityAdapterRepository;
import com.sixgroup.refit.observability.item35.creator.shared.DataTestUtils;
import com.sixgroup.refit.observability.item35.creator.shared.sla.SlaInfoRepository;
import com.sixgroup.refit.observability.modules.validate.domain.data.SlaInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.AppConstants.REGULATOR_ENTITY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegulatorServiceTest {

    @InjectMocks
    private RegulatorService regulatorService;

    @Mock
    private ReportingFileAdapterRepository reportingFileAdapterRepository;

    @Mock
    private RegulatorProperties fileTypeProperties;

    @Mock
    private ReportItemProperties reportProperties;

    @Mock
    private SlaInfoRepository slaInfoRepository;

    @Mock
    private ReguIdentityAdapterRepository reguIdentityAdapterRepository;

    @Mock
    private ReportEodProcessStateRepository reportEodProcessStateRepository;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(regulatorService, "blockSize", 1);

        ReportItemProperties.Translation translation = new ReportItemProperties.Translation();
        TranslationData translationData = new TranslationData();
        translationData.setName("eudrp0uu0000");
        translationData.setValue("EIOPA");

        translation.getAccounts().add(translationData);

        lenient().when(reportProperties.getTranslation()).thenReturn(translation);
    }

    @Test
    void findRegulators_repository_return_empty_list() {

        when(reportingFileAdapterRepository.iterateRegulatorByDayAccountAndFileType(anyString(), anyString()))
            .thenReturn(Collections.emptyIterator());

        List<ReportGenerationDto> response =
            regulatorService.findRegulator("2024-02-01", "2024-03-01", "20240215");

        assertTrue(response.isEmpty());

        verify(reportingFileAdapterRepository, times(1))
            .iterateRegulatorByDayAccountAndFileType(anyString(), anyString());
    }

    @Test
    void findRegulators_ok() {

        String fileName1 = "TRRGS_DATTSR_ESMAS_R15923-20240220_001001-0.zip";
        String reportType1 = "TAR108";
        LocalDateTime reportSessionDate1 = DataTestUtils.parseString("2024-02-20 18:55:23");
        String accountId1 = "eudritrace";
        LocalDateTime creationDate1 = DataTestUtils.parseString("2024-02-20 18:55:29");

        String fileName2 = "TRRGS_DATMDA_EUDRIRA1051_R60004-20240222_001001-0.zip";
        String reportType2 = "TSR107";
        LocalDateTime reportSessionDate2 = DataTestUtils.parseString("2024-02-20 18:55:23");
        String accountId2 = "eudritrace";
        LocalDateTime creationDate2 = DataTestUtils.parseString("2024-02-20 18:55:29");

        RegulatorDTO regulator1 =
            new RegulatorDTO(fileName1, reportType1, reportSessionDate1, accountId1, creationDate1, "CAESR");

        RegulatorDTO regulator2 =
            new RegulatorDTO(fileName2, reportType2, reportSessionDate2, accountId2, creationDate2, "CAFAA");

        SlaInfo slaInfo1 = SlaInfo.builder()
            .meetsSla(true)
            .expectSlaDate(reportSessionDate1.plusDays(1).truncatedTo(ChronoUnit.DAYS).plusHours(6))
            .generationDuration(Duration.ofMinutes(30))
            .build();

        SlaInfo slaInfo2 = SlaInfo.builder()
            .meetsSla(true)
            .expectSlaDate(reportSessionDate1.plusDays(1).truncatedTo(ChronoUnit.DAYS).plusHours(12))
            .generationDuration(Duration.ofMinutes(60))
            .build();

        ReportConfig reportConfig1 = new ReportConfig();
        reportConfig1.setName(reportType1);
        reportConfig1.setReportName(reportType1);

        ReportConfig reportConfig2 = new ReportConfig();
        reportConfig2.setName(reportType2);
        reportConfig2.setReportName(reportType2);

        when(fileTypeProperties.getReports()).thenReturn(List.of(reportConfig1, reportConfig2));

        when(reportingFileAdapterRepository.iterateRegulatorByDayAccountAndFileType(anyString(), anyString()))
            .thenReturn(List.of(regulator1, regulator2).iterator());

        when(slaInfoRepository.getSlaInfo(REGULATOR_ENTITY, reportType1, reportSessionDate1, creationDate1))
            .thenReturn(Optional.of(slaInfo1));

        when(slaInfoRepository.getSlaInfo(REGULATOR_ENTITY, reportType2, reportSessionDate2, creationDate2))
            .thenReturn(Optional.of(slaInfo2));

        when(reguIdentityAdapterRepository.findByTraceCode(any()))
            .thenReturn(List.of(
                ReguIdentityDTO.builder().traceCode("CAESR").regulatorId("eudri2frb000").build(),
                ReguIdentityDTO.builder().traceCode("CAFAA").regulatorId("eudri96jn000").build()
            ));

        when(reportEodProcessStateRepository.find(anyString(), anyString()))
            .thenReturn(Collections.emptyList());

        List<ReportGenerationDto> response =
            regulatorService.findRegulator("2024-02-01", "2024-03-01", "20240215");

        assertFalse(response.isEmpty());
        assertEquals(2, response.size());
    }

    @Test
    void findRegulators_ok_withEodInit() {

        String fileName1 = "TRRGS_DATTSR_ESMAS_R15923-20240220_001001-0.zip";
        String reportType1 = "TAR108";
        LocalDateTime reportSessionDate1 = DataTestUtils.parseString("2024-02-20 18:55:23");
        String accountId1 = "eudritrace";
        LocalDateTime creationDate1 = DataTestUtils.parseString("2024-02-20 18:55:29");

        RegulatorDTO regulator1 =
            new RegulatorDTO(fileName1, reportType1, reportSessionDate1, accountId1, creationDate1, "CAESR");

        SlaInfo slaInfo1 = SlaInfo.builder()
            .meetsSla(true)
            .expectSlaDate(reportSessionDate1.plusDays(1).truncatedTo(ChronoUnit.DAYS).plusHours(6))
            .generationDuration(Duration.ofMinutes(30))
            .build();

        ReportConfig reportConfig1 = new ReportConfig("TAR108", "TAR108", "MRAR000");

        ReportEoDDTO reportEo1 =
            new ReportEoDDTO("MRAR000", DataTestUtils.parseString("2024-02-20 02:00:00"), "2024-02-20");

        when(fileTypeProperties.getReports()).thenReturn(List.of(reportConfig1));

        when(reportingFileAdapterRepository.iterateRegulatorByDayAccountAndFileType(anyString(), anyString()))
            .thenReturn(List.of(regulator1).iterator());

        when(reportEodProcessStateRepository.find(anyString(), anyString()))
            .thenReturn(List.of(reportEo1));

        when(slaInfoRepository.getSlaInfo(REGULATOR_ENTITY, reportType1, reportSessionDate1, reportEo1.getStartedDate(), creationDate1))
            .thenReturn(Optional.of(slaInfo1));

        when(reguIdentityAdapterRepository.findByTraceCode(any()))
            .thenReturn(List.of(
                ReguIdentityDTO.builder().traceCode("CAESR").regulatorId("eudri2frb000").build()
            ));

        List<ReportGenerationDto> response =
            regulatorService.findRegulator("2024-02-01", "2024-03-01", "20240215");

        assertFalse(response.isEmpty());
        assertEquals(1, response.size());

        verify(reportEodProcessStateRepository, times(1))
            .find(anyString(), anyString());
    }
}
