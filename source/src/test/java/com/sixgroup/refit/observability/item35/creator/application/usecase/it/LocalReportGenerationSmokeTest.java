package com.sixgroup.refit.observability.item35.creator.application.usecase.it;

import com.sixgroup.refit.observability.ApplicationMain;
import com.sixgroup.refit.observability.item35.creator.application.service.ParticipantService;
import com.sixgroup.refit.observability.item35.creator.application.service.RegulatorService;
import com.sixgroup.refit.observability.item35.creator.application.service.TrService;
import com.sixgroup.refit.observability.item35.creator.application.usecase.UseCaseReportGeneration;
import com.sixgroup.refit.observability.item35.creator.configuration.CsvProperties;
import com.sixgroup.refit.observability.item35.creator.domain.enums.Command;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.domain.service.ProducerItemService;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(
    classes = ApplicationMain.class,
    properties = "spring.kafka.listener.auto-startup=false"
)
@ActiveProfiles({"test", "test-uk"})
class LocalReportGenerationSmokeTest {

    private static final String EXECUTION_DATE = "20260901";
    private static final String EXPECTED_FILE_NAME = "TRRGS_UKEMIR_PR_FU_ND_ITEM35B_20260815.csv";

    @Autowired
    private UseCaseReportGeneration useCaseReportGeneration;

    @Autowired
    private CsvProperties csvProperties;

    @MockBean
    private ParticipantService participantService;

    @MockBean
    private RegulatorService regulatorService;

    @MockBean
    private TrService trService;

    @MockBean
    private ProducerItemService producerItemService;

    @Test
    void starts_application_and_generates_monthly_report_csv() throws Exception {
        ReportGenerationDto row = new ReportGenerationDto(
            "2026-08-15", "TAR108", "PARTICIPANT", "2026-08-15T01:00:00Z",
            "2026-08-15T02:00:00Z", "2026-08-15T02:00:00Z", "2026-08-15",
            "2026-08-15T06:00:00Z", "0.0");

        when(participantService.iterateParticipants(anyString(), anyString(), anyString())).thenReturn(List.of(row).iterator());
        when(regulatorService.iterateRegulator(anyString(), anyString(), anyString())).thenReturn(Collections.emptyIterator());
        when(trService.iterateTr(anyString(), anyString(), anyString())).thenReturn(Collections.emptyIterator());

        ItemCommandDTO command = ItemCommandDTO.builder()
            .itemDate(EXECUTION_DATE)
            .itemType(ItemType.REPORT_GENERATION.getName())
            .command(Command.REQUEST.getDescription())
            .build();

        File generatedFile = useCaseReportGeneration.execute(command, new RecordHeaders());

        assertTrue(generatedFile.isFile());
        assertEquals(EXPECTED_FILE_NAME, generatedFile.getName());
        assertEquals(2, Files.readAllLines(generatedFile.toPath()).size());
        assertEquals(new File(csvProperties.getOutputPath(), EXPECTED_FILE_NAME).getCanonicalFile(),
            generatedFile.getCanonicalFile());
    }
}
