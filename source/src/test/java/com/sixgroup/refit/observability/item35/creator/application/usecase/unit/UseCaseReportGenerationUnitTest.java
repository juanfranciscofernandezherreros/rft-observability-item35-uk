package com.sixgroup.refit.observability.item35.creator.application.usecase.unit;

import com.sixgroup.refit.observability.item.state.application.StateService;
import com.sixgroup.refit.observability.item.state.domain.model.StateRequest;
import com.sixgroup.refit.observability.item35.creator.application.usecase.UseCaseReportGeneration;
import com.sixgroup.refit.observability.item35.creator.domain.enums.Command;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.domain.model.Storage;
import com.sixgroup.refit.observability.item35.creator.domain.model.StorageCapacityDto;
import com.sixgroup.refit.observability.item35.creator.domain.service.*;
import org.apache.kafka.common.header.Headers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class UseCaseReportGenerationUnitTest {

    @Captor
    ArgumentCaptor<List<ReportGenerationDto>> reportGenerationDtoListCaptor;
    @Mock
    private WriteFileItem35Service<ReportGenerationDto> writeFileReportGenerationService;
    @Mock
    private ProducerItemService producerItemService;
    @Mock
    private StateService stateService;
    @InjectMocks
    private UseCaseReportGeneration useCaseReportGeneration;
    @Mock
    private ParticipantService participantService;
    @Mock
    private RegulatorService regulatorService;
    @Mock
    private TrService trService;
    private final Headers mockHeaders = mock(Headers.class);

    @Test
    void getItemType() {
        assertEquals(useCaseReportGeneration.getItemType(), ItemType.REPORT_GENERATION);
    }

    @Test
    void execute_resource_not_found() {
        List<Storage> totalCapacityList = List.of(new Storage("2023-09-01T00:00:00.000Z", 16.703632f),
            new Storage("2023-09-02T00:00:00.000Z", 16.700466f));
        when(participantService.findParticipants(any(), any(), any())).thenReturn(Collections.emptyList());
        when(regulatorService.findRegulator(any(), any(), any())).thenReturn(Collections.emptyList());
        when(trService.findTr(any(), any(), any())).thenReturn(Collections.emptyList());

        RuntimeException runtimeException = assertThrows(RuntimeException.class,
            () -> useCaseReportGeneration.execute(getItemCommandDTO(), mockHeaders));
        assertEquals(runtimeException.getMessage(), "No data found in report generation, skipping report generation");
        verify(stateService, times(1)).setError(any());
        verify(producerItemService, times(0)).send(any(), any());
        verify(regulatorService, times(1)).findRegulator(any(), any(), any());
        verify(trService, times(1)).findTr(any(), any(), any());
    }

    @Test
    void execute() throws IOException {
        ReportGenerationDto reportGeneration_participant = new ReportGenerationDto("2024-02-29",
            "TAR108", "PARTICIPANT", "1900-01-01T00:00:06Z",
            "2024-02-20T18:55:29Z", "2024-02-20T18:55:29Z", "20-02-2024",
            "2024-02-21T06:00:00Z", "11.0");
        when(participantService.findParticipants(any(), any(), any())).thenReturn(List.of(reportGeneration_participant));

        ReportGenerationDto reportGeneration_regulator = new ReportGenerationDto("2024-02-29",
            "ESMAS-TSR107", "ESMA", "1900-01-01T18:55:29Z",
            "2024-02-20T18:55:29Z", "2024-02-20T18:55:29Z", "20-02-2024",
            "2024-02-21T12:00:00Z", "17.0");

        when(regulatorService.findRegulator(any(), any(), any())).thenReturn(List.of(reportGeneration_regulator));

        ReportGenerationDto reportGeneration_tr = new ReportGenerationDto("2024-02-29",
            "trkdp-TD", "TR", "1900-01-01T14:08:12Z",
            "2024-02-22T14:08:12Z", "2024-02-22T14:08:12Z", "22-02-2024",
            "2024-02-23T12:00:00Z", "21.8");

        when(trService.findTr(any(), any(), any())).thenReturn(List.of(reportGeneration_tr));

        File mockedFile = new File("test_file.csv");
        when(writeFileReportGenerationService.writeFile(anyList(), any())).thenReturn(mockedFile);

        ItemCommandDTO itemCommandDTO = getItemCommandDTO();

        File resultFile = useCaseReportGeneration.execute(itemCommandDTO, mockHeaders);
        assertNotNull(resultFile);
        verify(participantService, times(1)).findParticipants(any(), any(), any());
        verify(regulatorService, times(1)).findRegulator(any(), any(), any());
        verify(trService, times(1)).findTr(any(), any(), any());
        verify(stateService, times(1)).nextStep(any(StateRequest.class));
        verify(producerItemService, times(1)).send(any(), any());

        verify(writeFileReportGenerationService).writeFile(reportGenerationDtoListCaptor.capture(),
            any());
        List<ReportGenerationDto> value = reportGenerationDtoListCaptor.getValue();

        assertEquals(value, List.of(reportGeneration_participant, reportGeneration_regulator, reportGeneration_tr));
    }

    @Test
    void execute_throws_io_exception() throws IOException {
        ReportGenerationDto reportGeneration_participant = new ReportGenerationDto("2024-02-29",
            "TAR108", "PARTICIPANT", "1900-01-01T00:00:06Z",
            "2024-02-20T18:55:29Z", "2024-02-20T18:55:29Z", "20-02-2024",
            "2024-02-21T06:00:00Z", "11.0");
        when(participantService.findParticipants(any(), any(), any())).thenReturn(List.of(reportGeneration_participant));

        ReportGenerationDto reportGeneration_regulator = new ReportGenerationDto("2024-02-29",
            "ESMAS-TSR107", "ESMA", "1900-01-01T18:55:29Z",
            "2024-02-20T18:55:29Z", "2024-02-20T18:55:29Z", "20-02-2024",
            "2024-02-21T12:00:00Z", "17.0");

        when(regulatorService.findRegulator(any(), any(), any())).thenReturn(List.of(reportGeneration_regulator));

        ReportGenerationDto reportGeneration_tr = new ReportGenerationDto("2024-02-29",
            "trkdp-TD", "TR", "1900-01-01T14:08:12Z",
            "2024-02-22T14:08:12Z", "2024-02-22T14:08:12Z", "22-02-2024",
            "2024-02-23T12:00:00Z", "21.8");

        when(trService.findTr(any(), any(), any())).thenReturn(List.of(reportGeneration_tr));

        File mockedFile = new File("test_file.csv");
        when(writeFileReportGenerationService.writeFile(anyList(), any())).thenReturn(mockedFile);

        when(writeFileReportGenerationService.writeFile(anyList(), any())).thenThrow(new IOException("Error"));
        RuntimeException runtimeException = assertThrows(RuntimeException.class,
            () -> useCaseReportGeneration.execute(getItemCommandDTO(), mockHeaders));

        assertEquals(runtimeException.getMessage(), "java.io.IOException: Error");

        verify(participantService, times(1)).findParticipants(any(), any(), any());
        verify(regulatorService, times(1)).findRegulator(any(), any(), any());
        verify(trService, times(1)).findTr(any(), any(), any());
        verify(stateService, times(1)).nextStep(any(StateRequest.class));

        verify(writeFileReportGenerationService).writeFile(reportGenerationDtoListCaptor.capture(),
            any());
        List<ReportGenerationDto> value = reportGenerationDtoListCaptor.getValue();

        assertEquals(value, List.of(reportGeneration_participant, reportGeneration_regulator, reportGeneration_tr));
    }

    private static ItemCommandDTO getItemCommandDTO() {
        return ItemCommandDTO.builder()
            .itemDate("20240915")
            .itemType(ItemType.STORAGE_CAPACITY.getName())
            .command(Command.REQUEST.getDescription())
            .build();
    }

}
