package com.sixgroup.refit.observability.item35.creator.application.service.unit;

import com.sixgroup.refit.observability.item35.creator.application.service.RegulatorServiceImpl;
import com.sixgroup.refit.observability.item35.creator.configuration.RegulatorFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.domain.repository.ReportingFileRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.RegulatorDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.mappper.ParticipantMapper;
import com.sixgroup.refit.observability.item35.creator.infrastructure.mappper.RegulatorMapper;
import org.jetbrains.annotations.NotNull;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegulatorServiceImplTest {

    @InjectMocks
    private RegulatorServiceImpl regulatorService;
    @Mock
    private ReportingFileRepository reportingFileRepository;
    @Mock
    private RegulatorMapper regulatorMapper;

    @Test
    void findRegulators_repository_return_empty_list() {

        doReturn(List.of()).when(reportingFileRepository)
            .findRegulatorByDayAccountAndFileType(anyString(), anyString());

        assertEquals(regulatorService.findRegulator("2024-02-01", "2024-03-01", "20240229"),
            Collections.emptyList());

        verify(reportingFileRepository, times(1)).findRegulatorByDayAccountAndFileType(anyString(), anyString());

    }

    @Test
    void findRegulators_ok() {

        RegulatorDTO regulator_1 = new RegulatorDTO("TRRGS_DATTSR_ESMAS_R15923-20240220_001001-0.zip", "TSR107",
            Timestamp.valueOf("2024-02-20 18:55:23.512"), "eudritrace", Timestamp.valueOf("2024-02-20 18:55:29.771"));

        RegulatorDTO regulator_2 = new RegulatorDTO("TRRGS_DATMDA_EUDRIRA1051_R60004-20240222_001001-0.zip", "TAR108",
            Timestamp.valueOf("2024-02-22 19:13:44.223"), "eudrira1051", Timestamp.valueOf("2024-02-22 19:13:44.223"));

        doReturn(List.of(regulator_1, regulator_2)).when(reportingFileRepository)
            .findRegulatorByDayAccountAndFileType(anyString(), anyString());

        doReturn(getExpectedValue(regulator_1)).when(regulatorMapper)
            .toReportGenerationDto(any(), any());

        List<ReportGenerationDto> regulators = regulatorService
            .findRegulator("2024-02-01", "2024-03-01", "20240229");

        verify(reportingFileRepository,
            times(1)).findRegulatorByDayAccountAndFileType(anyString(), anyString());

        assertNotEquals(regulatorService.findRegulator("2024-02-01", "2024-03-01", "20240229"),
            Collections.emptyList());

        verify(regulatorMapper,
            times(4)).toReportGenerationDto(any(), any());

    }

    private static ReportGenerationDto getExpectedValue(RegulatorDTO regulator_1) {
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

        return new ReportGenerationDto(
            "2024-02-29",
            "ESMAS-TSR107",
            "ESMA",
            reportGenerationTimeString,
            reportCompletionAndPubTime,
            reportCompletionAndPubTime,
            date,
            sla,
            calculateDifference(regulator_1));
    }

    private static String calculateDifference(RegulatorDTO regulatorDTO) {
        OffsetDateTime creationDateOffsetDateTime = regulatorDTO.getCreationDate().toLocalDateTime()
            .truncatedTo(ChronoUnit.SECONDS).atOffset(ZoneOffset.UTC);
        OffsetDateTime slaDateOffsetDateTime = regulatorDTO.getReportingSession().toLocalDateTime().truncatedTo(ChronoUnit.DAYS)
            .atOffset(ZoneOffset.UTC).plusDays(1).plusHours(12);

        if (creationDateOffsetDateTime.isAfter(slaDateOffsetDateTime)) {
            // NEXT DAY FROM reportingSession
            slaDateOffsetDateTime = regulatorDTO.getReportingSession().toLocalDateTime().atOffset(ZoneOffset.UTC).plusDays(1);
        }

        // REPORT_PUBLICATION_TIME (NOW IS USING creationDate) - SLA
        // The format is a float that indicates the difference time between this two dates.
        float seconds = Duration.between(regulatorDTO.getCreationDate().toLocalDateTime().atOffset(ZoneOffset.UTC),
            slaDateOffsetDateTime).getSeconds() / 3600.00f;
        return new BigDecimal(seconds).setScale(1, RoundingMode.DOWN).toString();
    }

}
