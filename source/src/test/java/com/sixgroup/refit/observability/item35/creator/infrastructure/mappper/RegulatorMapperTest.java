package com.sixgroup.refit.observability.item35.creator.infrastructure.mappper;

import com.sixgroup.refit.observability.item35.creator.configuration.RegulatorProperties;
import com.sixgroup.refit.observability.item35.creator.domain.config.ReportConfig;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReguIdentityDTO;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.RegulatorDTO;
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
import java.util.HashMap;
import java.util.Map;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.AppConstants.DATE_FORMAT_YYYY_MM_DD;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class RegulatorMapperTest {

    @Test
    void with_ESMA_report_type() {
        final String fileName = "TRRGS_DATTSR_ESMAS_R15923-20240220_001001-0.zip";
        final String reportType = "TSR107";
        final LocalDateTime reportSessionDate = DataTestUtils.parseString("2024-02-20 18:55:23");
        final String accountId = "eudritrace";
        final LocalDateTime creationDate = DataTestUtils.parseString("2024-02-20 18:55:29");

        final RegulatorProperties fileTypeProperties = new RegulatorProperties();
        fileTypeProperties.setReportTypeEsma("ESMA");
        final ReportConfig reportConfig = new ReportConfig();
        reportConfig.setName(reportType);
        reportConfig.setReportName(reportType);
        fileTypeProperties.getReports().add(reportConfig);

        final RegulatorDTO regulatorDTO = new RegulatorDTO(fileName, reportType, reportSessionDate, accountId, creationDate, "");

        final SlaInfo slaInfo = SlaInfo.builder()
            .meetsSla(Boolean.TRUE)
            .expectSlaDate(reportSessionDate.plusDays(1).truncatedTo(ChronoUnit.DAYS).plusHours(6))
            .generationDuration(Duration.ofMinutes(30))
            .build();

        final RegulatorMapper regulatorMapper = new RegulatorMapper();
        final ReportGenerationDto response = regulatorMapper.toReportGenerationDto(regulatorDTO, fileTypeProperties, slaInfo, new HashMap<>());

        assertEquals("ESMA", response.getReportType());
    }

    @Test
    void with_NCA_report_type() {
        final String fileName = "TRRGS_DATTAR_CAESR_R99998-20240220_001001-0.zip";
        final String reportType = "TSR107";
        final LocalDateTime reportSessionDate = DataTestUtils.parseString("2024-02-20 18:55:23");
        final String accountId = "eudritrace";
        final LocalDateTime creationDate = DataTestUtils.parseString("2024-02-20 18:55:29");

        final RegulatorProperties fileTypeProperties = new RegulatorProperties();
        fileTypeProperties.setReportTypeNca("NCA");
        final ReportConfig reportConfig = new ReportConfig();
        reportConfig.setName(reportType);
        reportConfig.setReportName(reportType);
        fileTypeProperties.getReports().add(reportConfig);

        final RegulatorDTO regulatorDTO = new RegulatorDTO(fileName, reportType, reportSessionDate, accountId, creationDate, "");

        final SlaInfo slaInfo = SlaInfo.builder()
            .meetsSla(Boolean.TRUE)
            .expectSlaDate(reportSessionDate.plusDays(1).truncatedTo(ChronoUnit.DAYS).plusHours(6))
            .generationDuration(Duration.ofMinutes(30))
            .build();

        final RegulatorMapper regulatorMapper = new RegulatorMapper();
        final ReportGenerationDto response = regulatorMapper.toReportGenerationDto(regulatorDTO, fileTypeProperties, slaInfo, new HashMap<>());

        assertEquals("NCA", response.getReportType());
    }

    @Test
    void expected_file_name_trace_account() {
        final String fileName = "TRRGS_DATTAR_CAFAA_R99998-20240220_001001-0.zip";
        final String reportType = "TSR107";
        final LocalDateTime reportSessionDate = DataTestUtils.parseString("2024-02-20 18:55:23");
        final String accountId = "eudritrace";
        final LocalDateTime creationDate = DataTestUtils.parseString("2024-02-20 18:55:29");

        final RegulatorProperties fileTypeProperties = new RegulatorProperties();
        fileTypeProperties.setReportTypeNca("NCA");
        final ReportConfig reportConfig = new ReportConfig();
        reportConfig.setName(reportType);
        reportConfig.setReportName(reportType);
        fileTypeProperties.getReports().add(reportConfig);

        final RegulatorDTO regulatorDTO = new RegulatorDTO(fileName, reportType, reportSessionDate, accountId, creationDate, "CAFAA");

        final SlaInfo slaInfo = SlaInfo.builder()
            .meetsSla(Boolean.TRUE)
            .expectSlaDate(reportSessionDate.plusDays(1).truncatedTo(ChronoUnit.DAYS).plusHours(6))
            .generationDuration(Duration.ofMinutes(30))
            .build();

        final ReguIdentityDTO dto1 = ReguIdentityDTO.builder().traceCode("CAFAA").regulatorId("eudri2frb777").traceConnectivity(false).build();
        final ReguIdentityDTO dto2 = ReguIdentityDTO.builder().traceCode("CAESR").regulatorId("eudri96jn000").traceConnectivity(false).build();
        final Map<String, ReguIdentityDTO> traceCodeRegulatorMap = new HashMap();
        traceCodeRegulatorMap.put("CAFAA", dto1);
        traceCodeRegulatorMap.put("CAESR", dto2);

        final RegulatorMapper regulatorMapper = new RegulatorMapper();
        final ReportGenerationDto response = regulatorMapper.toReportGenerationDto(regulatorDTO, fileTypeProperties, slaInfo, traceCodeRegulatorMap);

        assertEquals("CAFAA-TSR107 TRACE", response.getReportName());
    }

    @Test
    void expected_file_name_trace_connectivity() {
        final String fileName = "TRRGS_DATTAR_CAFAA_R99998-20240220_001001-0.zip";
        final String reportType = "TSR107";
        final LocalDateTime reportSessionDate = DataTestUtils.parseString("2024-02-20 18:55:23");
        final String accountId = "eudri2frb777";
        final LocalDateTime creationDate = DataTestUtils.parseString("2024-02-20 18:55:29");

        final RegulatorProperties fileTypeProperties = new RegulatorProperties();
        fileTypeProperties.setReportTypeNca("NCA");
        final ReportConfig reportConfig = new ReportConfig();
        reportConfig.setName(reportType);
        reportConfig.setReportName(reportType);
        fileTypeProperties.getReports().add(reportConfig);

        final RegulatorDTO regulatorDTO = new RegulatorDTO(fileName, reportType, reportSessionDate, accountId, creationDate, "CAFAA");

        final SlaInfo slaInfo = SlaInfo.builder()
            .meetsSla(Boolean.TRUE)
            .expectSlaDate(reportSessionDate.plusDays(1).truncatedTo(ChronoUnit.DAYS).plusHours(6))
            .generationDuration(Duration.ofMinutes(30))
            .build();

        final ReguIdentityDTO dto1 = ReguIdentityDTO.builder().traceCode("CAFAA").regulatorId("eudri2frb777").traceConnectivity(true).build();
        final ReguIdentityDTO dto2 = ReguIdentityDTO.builder().traceCode("CAESR").regulatorId("eudri96jn000").traceConnectivity(false).build();
        final Map<String, ReguIdentityDTO> traceCodeRegulatorMap = new HashMap();
        traceCodeRegulatorMap.put("CAFAA", dto1);
        traceCodeRegulatorMap.put("CAESR", dto2);

        final RegulatorMapper regulatorMapper = new RegulatorMapper();
        final ReportGenerationDto response = regulatorMapper.toReportGenerationDto(regulatorDTO, fileTypeProperties, slaInfo, traceCodeRegulatorMap);

        assertEquals("CAFAA-TSR107 TRACE", response.getReportName());
    }

    @Test
    void expected_file_name_accountId() {
        final String fileName = "TRRGS_DATTAR_CAFAA_R99998-20240220_001001-0.zip";
        final String reportType = "TSR107";
        final LocalDateTime reportSessionDate = DataTestUtils.parseString("2024-02-20 18:55:23");
        final String accountId = "eudrira1051";
        final LocalDateTime creationDate = DataTestUtils.parseString("2024-02-20 18:55:29");

        final RegulatorProperties fileTypeProperties = new RegulatorProperties();
        fileTypeProperties.setReportTypeNca("NCA");
        final ReportConfig reportConfig = new ReportConfig();
        reportConfig.setName(reportType);
        reportConfig.setReportName(reportType);
        fileTypeProperties.getReports().add(reportConfig);

        final RegulatorDTO regulatorDTO = new RegulatorDTO(fileName, reportType, reportSessionDate, accountId, creationDate, "CAFAA");

        final SlaInfo slaInfo = SlaInfo.builder()
            .meetsSla(Boolean.TRUE)
            .expectSlaDate(reportSessionDate.plusDays(1).truncatedTo(ChronoUnit.DAYS).plusHours(6))
            .generationDuration(Duration.ofMinutes(30))
            .build();

        final ReguIdentityDTO dto1 = ReguIdentityDTO.builder().traceCode("CAFAA").regulatorId("eudrira1051").traceConnectivity(false).build();
        final ReguIdentityDTO dto2 = ReguIdentityDTO.builder().traceCode("CAESR").regulatorId("eudri96jn000").traceConnectivity(false).build();
        final Map<String, ReguIdentityDTO> traceCodeRegulatorMap = new HashMap();
        traceCodeRegulatorMap.put("CAFAA", dto1);
        traceCodeRegulatorMap.put("CAESR", dto2);

        final RegulatorMapper regulatorMapper = new RegulatorMapper();
        final ReportGenerationDto response = regulatorMapper.toReportGenerationDto(regulatorDTO, fileTypeProperties, slaInfo, traceCodeRegulatorMap);

        assertEquals("eudrira1051-TSR107 Portal XML", response.getReportName());
    }

    @Test
    void expected_file_name_accountId_when_tracecode_not_exists() {
        final String fileName = "TRRGS_DATTAR_CAFAA_R99998-20240220_001001-0.zip";
        final String reportType = "TSR107";
        final LocalDateTime reportSessionDate = DataTestUtils.parseString("2024-02-20 18:55:23");
        final String accountId = "eudrira1051";
        final LocalDateTime creationDate = DataTestUtils.parseString("2024-02-20 18:55:29");

        final RegulatorProperties fileTypeProperties = new RegulatorProperties();
        fileTypeProperties.setReportTypeNca("NCA");
        final ReportConfig reportConfig = new ReportConfig();
        reportConfig.setName(reportType);
        reportConfig.setReportName(reportType);
        fileTypeProperties.getReports().add(reportConfig);

        final RegulatorDTO regulatorDTO = new RegulatorDTO(fileName, reportType, reportSessionDate, accountId, creationDate, "CAFAA");

        final SlaInfo slaInfo = SlaInfo.builder()
            .meetsSla(Boolean.TRUE)
            .expectSlaDate(reportSessionDate.plusDays(1).truncatedTo(ChronoUnit.DAYS).plusHours(6))
            .generationDuration(Duration.ofMinutes(30))
            .build();

        final Map<String, ReguIdentityDTO> traceCodeRegulatorMap = new HashMap();

        final RegulatorMapper regulatorMapper = new RegulatorMapper();
        final ReportGenerationDto response = regulatorMapper.toReportGenerationDto(regulatorDTO, fileTypeProperties, slaInfo, traceCodeRegulatorMap);

        assertEquals("eudrira1051-TSR107 Portal XML", response.getReportName());
    }


    @Test
    void findRegulators_ok() {
        final String fileName = "TRRGS_DATTSR_ESMAS_R15923-20240220_001001-0.zip";
        final String reportType = "TSR107";
        final LocalDateTime reportSessionDate = DataTestUtils.parseString("2024-02-20 18:55:23");
        final String accountId = "eudritrace";
        final LocalDateTime creationDate = DataTestUtils.parseString("2024-02-20 18:55:29");

        final RegulatorProperties fileTypeProperties = new RegulatorProperties();
        fileTypeProperties.setReportTypeEsma("ESMA");
        final ReportConfig reportConfig = new ReportConfig();
        reportConfig.setName(reportType);
        reportConfig.setReportName(reportType);
        fileTypeProperties.getReports().add(reportConfig);

        final RegulatorDTO regulatorDTO = new RegulatorDTO(fileName, reportType, reportSessionDate, accountId, creationDate, "CAESR");

        final SlaInfo slaInfo = SlaInfo.builder()
            .meetsSla(Boolean.TRUE)
            .expectSlaDate(reportSessionDate.plusDays(1).truncatedTo(ChronoUnit.DAYS).plusHours(12))
            .generationDuration(Duration.ofMinutes(18 * 60 + 55).plusSeconds(29))
            .build();

        final ReguIdentityDTO dto1 = ReguIdentityDTO.builder().traceCode("CAFAA").regulatorId("eudri2frb777").traceConnectivity(false).build();
        final ReguIdentityDTO dto2 = ReguIdentityDTO.builder().traceCode("CAESR").regulatorId("eudri96jn000").traceConnectivity(false).build();
        final Map<String, ReguIdentityDTO> traceCodeRegulatorMap = new HashMap();
        traceCodeRegulatorMap.put("CAFAA", dto1);
        traceCodeRegulatorMap.put("CAESR", dto2);

        final RegulatorMapper regulatorMapper = new RegulatorMapper();
        final ReportGenerationDto response = regulatorMapper.toReportGenerationDto(regulatorDTO, fileTypeProperties, slaInfo, traceCodeRegulatorMap);

        OffsetDateTime originDate = OffsetDateTime
            .of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        OffsetDateTime offsetDateTimeCreationDate = regulatorDTO.getCreationDate().atOffset(ZoneOffset.UTC);

        String reportGenerationTimeString = originDate
            .withHour(offsetDateTimeCreationDate.getHour())
            .withMinute(offsetDateTimeCreationDate.getMinute())
            .withSecond(offsetDateTimeCreationDate.getSecond()).format(DateTimeFormatter.ISO_INSTANT);


        String reportCompletionAndPubTime = regulatorDTO.getCreationDate().truncatedTo(ChronoUnit.SECONDS)
            .atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);

        String date = regulatorDTO.getReportingSession()
            .format(DateTimeFormatter.ofPattern(DATE_FORMAT_YYYY_MM_DD));

        String sla = regulatorDTO.getReportingSession().truncatedTo(ChronoUnit.DAYS)
            .atOffset(ZoneOffset.UTC).plusDays(1).plusHours(12).format((DateTimeFormatter.ISO_INSTANT));

        ReportGenerationDto expectedValue = new ReportGenerationDto(
            null,
            "CAESR-TSR107 TRACE",
            "ESMA",
            reportGenerationTimeString,
            reportCompletionAndPubTime,
            null,
            date,
            sla, null);

        assertEquals(expectedValue, response);
    }
}
