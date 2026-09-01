package com.sixgroup.refit.observability.item35.creator.application.usecase.unit;

import com.sixgroup.refit.observability.item.state.application.StateService;
import com.sixgroup.refit.observability.item.state.domain.model.StateRequest;
import com.sixgroup.refit.observability.item35.creator.application.service.FileNameService;
import com.sixgroup.refit.observability.item35.creator.application.service.ParticipantService;
import com.sixgroup.refit.observability.item35.creator.application.service.RegulatorService;
import com.sixgroup.refit.observability.item35.creator.application.service.TrService;
import com.sixgroup.refit.observability.item35.creator.application.usecase.UseCaseReportGeneration;
import com.sixgroup.refit.observability.item35.creator.configuration.CsvProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.Regulation;
import com.sixgroup.refit.observability.item35.creator.configuration.ReportItemProperties;
import com.sixgroup.refit.observability.item35.creator.domain.enums.Command;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.domain.service.ProducerItemService;
import com.sixgroup.refit.observability.item35.creator.domain.service.WriteFileItem35Service;
import com.sixgroup.refit.observability.item35.creator.infrastructure.file.WriteFileReportGeneration;
import org.apache.kafka.common.header.Headers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UseCaseReportGenerationUnitTest {

    private final Headers mockHeaders = mock(Headers.class);
    @InjectMocks
    private UseCaseReportGeneration useCaseReportGeneration;
    @Captor
    ArgumentCaptor<List<ReportGenerationDto>> reportGenerationDtoListCaptor;
    @Mock
    private WriteFileItem35Service<ReportGenerationDto> writeFileReportGenerationService;
    @Mock
    private ProducerItemService producerItemService;
    @Mock
    private ParticipantService participantService;
    @Mock
    private RegulatorService regulatorService;
    @Mock
    private TrService trService;
    @Mock
    private FileNameService fileNameService;
    @Mock
    private StateService stateService;
    @Mock
    private ReportItemProperties reportItemProperties;

    @Test
    void getItemType() {
        assertEquals(ItemType.REPORT_GENERATION, useCaseReportGeneration.getItemType());
    }

    @Test
    void generates_item35b_csv_with_the_requested_reference_date(@TempDir Path outputDirectory) throws IOException {
        ReportItemProperties properties = new ReportItemProperties();
        properties.setRegulation(Regulation.UK);
        properties.setTrCode("TRRGS");
        properties.setRegulationReference("EMIR");
        properties.setReportGenerationFileNamePattern("TRRGS_UKEMIR_PR_FU_ND_ITEM35B_YYYYMMDD.csv");

        CsvProperties csvProperties = new CsvProperties();
        csvProperties.setOutputPath(outputDirectory.toString());

        FileNameService realFileNameService = new FileNameService(properties);
        WriteFileReportGeneration realCsvWriter = new WriteFileReportGeneration(csvProperties, properties);
        ItemCommandDTO command = ItemCommandDTO.builder()
            .itemDate("20260907")
            .itemType(ItemType.REPORT_GENERATION.getName())
            .command(Command.REQUEST.getDescription())
            .build();
        ReportGenerationDto row = new ReportGenerationDto(
            "2026-09-15", "TAR108", "PARTICIPANT", "1900-01-01T01:00:00Z",
            "2026-09-01T01:00:00Z", "2026-09-01T01:00:00Z", "2026-09-01",
            "2026-09-01T06:00:00Z", "0.0");

        String fileName = realFileNameService.getFileName(ItemType.REPORT_GENERATION, command.getItemDate());
        File generatedFile = realCsvWriter.writeFile(List.of(row), command, fileName);

        assertEquals("TRRGS_UKEMIR_PR_FU_ND_ITEM35B_20260815.csv", generatedFile.getName());
        assertTrue(generatedFile.isFile());
        assertEquals(outputDirectory.resolve(fileName).toFile(), generatedFile);
        assertEquals(2, Files.readAllLines(generatedFile.toPath()).size());
    }

    @ParameterizedTest
    @CsvSource({
        "SUBMISSION_VOLUMES, 20260901, TRRGS_UKEMIR_PR_FU_ND_ITEM35A_20260815.csv",
        "REPORT_GENERATION, 20260907, TRRGS_UKEMIR_PR_FU_ND_ITEM35B_20260815.csv",
        "STORAGE_CAPACITY, 20260915, TRRGS_UKEMIR_PR_FU_ND_ITEM35C_20260815.csv",
        "COMPUTE_CAPACITY, 20260930, TRRGS_UKEMIR_PR_FU_ND_ITEM35D_20260815.csv"
    })
    void uses_the_same_previous_month_reference_date_for_all_item35_files(
        ItemType itemType, String executionDate, String expectedFileName) {
        FileNameService realFileNameService = new FileNameService(ukReportProperties());

        assertEquals(expectedFileName, realFileNameService.getFileName(itemType, executionDate));
    }

    @ParameterizedTest
    @CsvSource({
        "20260107, TRRGS_UKEMIR_PR_FU_ND_ITEM35B_20251215.csv",
        "20260228, TRRGS_UKEMIR_PR_FU_ND_ITEM35B_20260115.csv",
        "20260301, TRRGS_UKEMIR_PR_FU_ND_ITEM35B_20260215.csv",
        "20260907, TRRGS_UKEMIR_PR_FU_ND_ITEM35B_20260815.csv",
        "20261231, TRRGS_UKEMIR_PR_FU_ND_ITEM35B_20261115.csv"
    })
    void generates_the_previous_month_reference_date_throughout_the_year(
        String executionDate, String expectedFileName) {
        FileNameService realFileNameService = new FileNameService(ukReportProperties());

        assertEquals(expectedFileName,
            realFileNameService.getFileName(ItemType.REPORT_GENERATION, executionDate));
    }

    @Test
    void execute_resource_not_found() {
        final String fileName = "test_file.csv";

        when(participantService.findParticipants(any(), any(), any())).thenReturn(Collections.emptyList());
        when(regulatorService.findRegulator(any(), any(), any())).thenReturn(Collections.emptyList());
        when(trService.findTr(any(), any(), any())).thenReturn(Collections.emptyList());
        when(fileNameService.getFileName(any(), any())).thenReturn(fileName);

        final File response = useCaseReportGeneration.execute(getItemCommandDTO(), mockHeaders);

        assertNull(response);
        verify(stateService, times(1)).setError(any());
        verify(producerItemService, times(0)).send(any(), any());
        verify(regulatorService, times(1)).findRegulator(any(), any(), any());
        verify(trService, times(1)).findTr(any(), any(), any());
    }

    @Test
    void execute() throws IOException {
        final String fileName = "test_file.csv";

        final ReportGenerationDto reportGenerationParticipant = new ReportGenerationDto("2024-02-29",
            "TAR108", "participant", "1900-01-01T00:00:06Z",
            "2024-02-20T18:55:29Z", "2024-02-20T18:55:29Z", "2024-02-20",
            "2024-02-21T06:00:00Z", "11.0");
        when(participantService.findParticipants(any(), any(), any())).thenReturn(List.of(reportGenerationParticipant));

        final ReportGenerationDto reportGenerationRegulator = new ReportGenerationDto("2024-02-29",
            "TSR107", "ESMA", "1900-01-01T18:55:29Z",
            "2024-02-20T18:55:29Z", "2024-02-20T18:55:29Z", "2024-02-20",
            "2024-02-21T12:00:00Z", "17.0");

        when(regulatorService.findRegulator(any(), any(), any())).thenReturn(List.of(reportGenerationRegulator));

        ReportGenerationDto reportGenerationTr = new ReportGenerationDto("2024-02-29",
            "RL078", "TR", "1900-01-01T14:08:12Z",
            "2024-02-22T14:08:12Z", "2024-02-22T14:08:12Z", "2024-02-22",
            "2024-02-23T12:00:00Z", "21.8");

        when(trService.findTr(any(), any(), any())).thenReturn(List.of(reportGenerationTr));

        final File mockedFile = new File(fileName);
        when(writeFileReportGenerationService.writeFile(anyList(), any(), any())).thenReturn(mockedFile);
        when(fileNameService.getFileName(any(), any())).thenReturn(fileName);

        final ItemCommandDTO itemCommandDTO = getItemCommandDTO();

        final File resultFile = useCaseReportGeneration.execute(itemCommandDTO, mockHeaders);
        assertNotNull(resultFile);
        verify(participantService, times(1)).findParticipants(any(), any(), any());
        verify(regulatorService, times(1)).findRegulator(any(), any(), any());
        verify(trService, times(1)).findTr(any(), any(), any());
        verify(stateService, times(4)).nextStep(any(StateRequest.class));
        verify(producerItemService, times(1)).send(any(), any());

        verify(writeFileReportGenerationService).writeFile(reportGenerationDtoListCaptor.capture(),
            any(), any());
        List<ReportGenerationDto> value = reportGenerationDtoListCaptor.getValue();

        assertEquals(value, List.of(reportGenerationParticipant, reportGenerationRegulator, reportGenerationTr));
    }

    @Test
    void execute_throws_io_exception() throws IOException {
        final String fileName = "test_file.csv";
        ReportGenerationDto reportGeneration_participant = new ReportGenerationDto("2024-02-29",
            "TAR108", "participant", "1900-01-01T00:00:06Z",
            "2024-02-20T18:55:29Z", "2024-02-20T18:55:29Z", "20-02-2024",
            "2024-02-21T06:00:00Z", "11.0");
        when(participantService.findParticipants(any(), any(), any())).thenReturn(List.of(reportGeneration_participant));

        ReportGenerationDto reportGeneration_regulator = new ReportGenerationDto("2024-02-29",
            "TSR107", "ESMA", "1900-01-01T18:55:29Z",
            "2024-02-20T18:55:29Z", "2024-02-20T18:55:29Z", "20-02-2024",
            "2024-02-21T12:00:00Z", "17.0");

        when(regulatorService.findRegulator(any(), any(), any())).thenReturn(List.of(reportGeneration_regulator));

        ReportGenerationDto reportGeneration_tr = new ReportGenerationDto("2024-02-29",
            "RL078", "TR", "1900-01-01T14:08:12Z",
            "2024-02-22T14:08:12Z", "2024-02-22T14:08:12Z", "22-02-2024",
            "2024-02-23T12:00:00Z", "21.8");

        when(trService.findTr(any(), any(), any())).thenReturn(List.of(reportGeneration_tr));

        File mockedFile = new File(fileName);
        when(writeFileReportGenerationService.writeFile(anyList(), any(), any())).thenReturn(mockedFile);

        when(writeFileReportGenerationService.writeFile(anyList(), any(), any())).thenThrow(new IOException("Error"));
        when(fileNameService.getFileName(any(), any())).thenReturn(fileName);

        final File response = useCaseReportGeneration.execute(getItemCommandDTO(), mockHeaders);

        assertNull(response);

        verify(participantService, times(1)).findParticipants(any(), any(), any());
        verify(regulatorService, times(1)).findRegulator(any(), any(), any());
        verify(trService, times(1)).findTr(any(), any(), any());
        verify(stateService, times(2)).nextStep(any(StateRequest.class));

        verify(writeFileReportGenerationService).writeFile(reportGenerationDtoListCaptor.capture(),
            any(), any());
        List<ReportGenerationDto> value = reportGenerationDtoListCaptor.getValue();

        assertEquals(value, List.of(reportGeneration_participant, reportGeneration_regulator, reportGeneration_tr));
    }

    private ItemCommandDTO getItemCommandDTO() {
        return ItemCommandDTO.builder()
            .itemDate("20240915")
            .itemType(ItemType.REPORT_GENERATION.getName())
            .command(Command.REQUEST.getDescription())
            .build();
    }

    private ReportItemProperties ukReportProperties() {
        ReportItemProperties properties = new ReportItemProperties();
        properties.setRegulation(Regulation.UK);
        properties.setSubmissionVolumesFileNamePattern("TRRGS_UKEMIR_PR_FU_ND_ITEM35A_YYYYMMDD.csv");
        properties.setReportGenerationFileNamePattern("TRRGS_UKEMIR_PR_FU_ND_ITEM35B_YYYYMMDD.csv");
        properties.setStorageCapacityFileNamePattern("TRRGS_UKEMIR_PR_FU_ND_ITEM35C_YYYYMMDD.csv");
        properties.setComputeCapacityFileNamePattern("TRRGS_UKEMIR_PR_FU_ND_ITEM35D_YYYYMMDD.csv");
        return properties;
    }

}
