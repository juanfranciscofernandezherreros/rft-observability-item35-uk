package com.sixgroup.refit.observability.item35.creator.infrastructure.mappper;

import com.sixgroup.refit.observability.item35.creator.configuration.ParticipantFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.RegulatorFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.RegulatorDTO;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegulatorMapperTest {

    @Mock
    private RegulatorFileTypeProperties fileTypeProperties;

    @Test
    void with_ESMA_report_type() {
        RegulatorDTO regulator_1 = new RegulatorDTO("TRRGS_DATTSR_ESMAS_R15923-20240220_001001-0.zip", "TSR107",
            Timestamp.valueOf("2024-02-20 18:55:23.512"), "eudritrace", Timestamp.valueOf("2024-02-20 18:55:29.771"));

        when(fileTypeProperties.getREPORT_TYPE_ESMA()).thenReturn("ESMA");
        when(fileTypeProperties.getTYPES())
            .thenReturn(Map.of("TSR107.REPORT_NAME", "TSR107",
                "TSR107.SLA", "yyyy-MM-ddT12:00:00Z",
                "TAR108.REPORT_NAME", "TAR108",
                "TAR108.SLA", "yyyy-MM-ddT12:00:00Z"));

        RegulatorMapper regulatorMapper = mock(RegulatorMapper.class, Mockito.CALLS_REAL_METHODS);

        ReportGenerationDto result = new ReportGenerationDto();
            regulatorMapper.manageData(regulator_1, fileTypeProperties, result);

        assertEquals("ESMA", result.getReportType());

        verify(regulatorMapper, times(1))
            .manageData(any(), any(), any());
    }

    @Test
    void with_NCA_report_type() {
        RegulatorDTO regulator_1 = new RegulatorDTO("TRRGS_DATTSR_ESMAS_R15923-20240220_001001-0.zip", "TSR107",
            Timestamp.valueOf("2024-02-20 18:55:23.512"), "eudritrace", Timestamp.valueOf("2024-02-20 18:55:29.771"));

        when(fileTypeProperties.getREPORT_TYPE_ESMA()).thenReturn("NCA");
        when(fileTypeProperties.getTYPES())
            .thenReturn(Map.of("TSR107.REPORT_NAME", "TSR107",
                "TSR107.SLA", "yyyy-MM-ddT12:00:00Z",
                "TAR108.REPORT_NAME", "TAR108",
                "TAR108.SLA", "yyyy-MM-ddT12:00:00Z"));

        RegulatorMapper regulatorMapper = mock(RegulatorMapper.class, Mockito.CALLS_REAL_METHODS);

        ReportGenerationDto result = new ReportGenerationDto();
        regulatorMapper.manageData(regulator_1, fileTypeProperties, result);

        assertEquals("NCA", result.getReportType());

        verify(regulatorMapper, times(1)).manageData(any(), any(), any());
    }


    @Test
    void expected_file_name() {
        RegulatorDTO regulator_1 = new RegulatorDTO("TRRGS_DATTAR_CAESR_R99998-240301_001001-0.zip", "TSR107",
            Timestamp.valueOf("2024-02-20 18:55:23.512"), "eudritrace", Timestamp.valueOf("2024-02-20 18:55:29.771"));

        when(fileTypeProperties.getREPORT_TYPE_NCA()).thenReturn("NCA");
        when(fileTypeProperties.getTYPES())
            .thenReturn(Map.of("TSR107.REPORT_NAME", "TSR107",
                "TSR107.SLA", "yyyy-MM-ddT12:00:00Z",
                "TAR108.REPORT_NAME", "TAR108",
                "TAR108.SLA", "yyyy-MM-ddT12:00:00Z"));

        RegulatorMapper regulatorMapper = mock(RegulatorMapper.class, Mockito.CALLS_REAL_METHODS);

        ReportGenerationDto result = new ReportGenerationDto();
        regulatorMapper.manageData(regulator_1, fileTypeProperties, result);


        assertEquals("CAESR-TSR107", result.getReportName());

        verify(regulatorMapper, times(1)).manageData(any(), any(), any());

    }

    @Test
    void findRegulators_ok() {
        RegulatorDTO regulator_1 = new RegulatorDTO("TRRGS_DATTSR_ESMAS_R15923-20240220_001001-0.zip", "TSR107",
            Timestamp.valueOf("2024-02-20 18:55:23.512"), "eudritrace", Timestamp.valueOf("2024-02-20 18:55:29.771"));

        when(fileTypeProperties.getREPORT_TYPE_ESMA()).thenReturn("ESMA");
        when(fileTypeProperties.getTYPES())
            .thenReturn(Map.of("TSR107.REPORT_NAME", "TSR107",
                "TSR107.SLA", "yyyy-MM-ddT06:00:00Z",
                "TAR108.REPORT_NAME", "TAR108",
                "TAR108.SLA", "yyyy-MM-ddT06:00:00Z"));

        RegulatorMapper regulatorMapper = mock(RegulatorMapper.class, Mockito.CALLS_REAL_METHODS);

        ReportGenerationDto result = new ReportGenerationDto();
        regulatorMapper.manageData(regulator_1, fileTypeProperties, result);

        OffsetDateTime originDate = OffsetDateTime
            .of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        OffsetDateTime offsetDateTimeCreationDate = regulator_1.getCreationDate().toLocalDateTime().atOffset(ZoneOffset.UTC);

        String reportGenerationTimeString = originDate
            .withHour(offsetDateTimeCreationDate.getHour())
            .withMinute(offsetDateTimeCreationDate.getMinute())
            .withSecond(offsetDateTimeCreationDate.getSecond()).format(DateTimeFormatter.ISO_INSTANT);


        String reportCompletionAndPubTime = regulator_1.getCreationDate().toLocalDateTime().truncatedTo(ChronoUnit.SECONDS)
            .atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);

        String date = regulator_1.getReportingSession().toLocalDateTime()
            .format(DateTimeFormatter.ofPattern(DATE_FORMAT_dd_MM_yyyy));

        String sla = regulator_1.getReportingSession().toLocalDateTime().truncatedTo(ChronoUnit.DAYS)
            .atOffset(ZoneOffset.UTC).plusDays(1).plusHours(12).format((DateTimeFormatter.ISO_INSTANT));

        ReportGenerationDto expectedValue = new ReportGenerationDto(
            null,
            "ESMAS-TSR107",
            "ESMA",
            reportGenerationTimeString,
            reportCompletionAndPubTime,
            reportCompletionAndPubTime,
            date,
            sla,
            calculateDifference(regulator_1));

        assertEquals(expectedValue, result);

        verify(regulatorMapper, times(1)).manageData(any(), any(), any());

    }

    private static String calculateDifference(RegulatorDTO regulatorDTO) {
        OffsetDateTime creationDateOffsetDateTime = regulatorDTO.getCreationDate().toLocalDateTime()
            .truncatedTo(ChronoUnit.SECONDS).atOffset(ZoneOffset.UTC);
        OffsetDateTime slaDateOffsetDateTime = regulatorDTO.getReportingSession().toLocalDateTime().truncatedTo(ChronoUnit.DAYS)
            .atOffset(ZoneOffset.UTC).plusDays(1).plusHours(12);

        if (creationDateOffsetDateTime.isAfter(slaDateOffsetDateTime)) {
            // NEXT DAY FROM reportingSession
            slaDateOffsetDateTime = regulatorDTO.getReportingSession().toLocalDateTime().atOffset(ZoneOffset.UTC).plusDays(1);
            // REPORT_PUBLICATION_TIME (NOW IS USING creationDate) - SLA
            // The format is a float that indicates the diference of time between this two dates.
            float seconds = Duration.between(regulatorDTO.getCreationDate().toLocalDateTime().atOffset(ZoneOffset.UTC),
                slaDateOffsetDateTime).getSeconds() / 3600.00f;
            return new BigDecimal(seconds).setScale(1, RoundingMode.DOWN).toString();
        }
        return "";
    }
}
