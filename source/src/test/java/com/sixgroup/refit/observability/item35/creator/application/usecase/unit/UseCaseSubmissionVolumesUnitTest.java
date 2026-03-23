package com.sixgroup.refit.observability.item35.creator.application.usecase.unit;

import com.sixgroup.refit.observability.item.state.application.StateService;
import com.sixgroup.refit.observability.item.state.domain.model.StateRequest;
import com.sixgroup.refit.observability.item35.creator.application.mock.ItemCommandMock;
import com.sixgroup.refit.observability.item35.creator.application.service.FileNameService;
import com.sixgroup.refit.observability.item35.creator.application.service.RecordStatusService;
import com.sixgroup.refit.observability.item35.creator.application.usecase.UseCaseSubmissionVolumes;
import com.sixgroup.refit.observability.item35.creator.configuration.ReportItemProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.RecordStatus;
import com.sixgroup.refit.observability.item35.creator.domain.service.ProducerItemService;
import com.sixgroup.refit.observability.item35.creator.domain.service.WriteFileItem35Service;
import org.apache.kafka.common.header.Headers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UseCaseSubmissionVolumesUnitTest {

    private final Headers mockHeaders = mock(Headers.class);
    @InjectMocks
    private UseCaseSubmissionVolumes useCaseSubmissionVolumes;
    @Mock
    private RecordStatusService recordStatusService;
    @Mock
    private WriteFileItem35Service writeFileSubmissionVolumesService;
    @Mock
    private ProducerItemService producerItemService;
    @Mock
    private FileNameService fileNameService;
    @Mock
    private StateService stateService;
    @Mock
    private ReportItemProperties reportItemProperties;

    @Test
    void testManageFileSubmissionVolumes_Success() throws Exception {
        final String fileName = "test_file.csv";
        List<RecordStatus> recordStatusList = new ArrayList<>();
        recordStatusList.add(new RecordStatus("2024-01-01", "test", "web", 10));
        File mockedFile = new File(fileName);

        when(recordStatusService.findRecordStatus(any(), any())).thenReturn(recordStatusList);
        when(writeFileSubmissionVolumesService.writeFile(anyList(), any(), any())).thenReturn(mockedFile);
        when(fileNameService.getFileName(any(), any())).thenReturn(fileName);

        File resultFile = useCaseSubmissionVolumes.execute(ItemCommandMock.builderItemCommand(), mockHeaders);
        assertNotNull(resultFile);
        verify(stateService, times(4)).nextStep((StateRequest) any());
        verify(producerItemService, times(1)).send(any(), any());
    }

    @Test
    void testManageFileSubmissionVolumes_NoRecordStatusFound() {
        final String fileName = "test_file.csv";

        when(recordStatusService.findRecordStatus(any(), any())).thenReturn(new ArrayList<>());
        when(fileNameService.getFileName(any(), any())).thenReturn(fileName);

        File resultFile = useCaseSubmissionVolumes.execute(ItemCommandMock.builderItemCommand(), mockHeaders);

        assertNull(resultFile);
        verify(stateService, times(1)).setError(any());
        verify(producerItemService, times(0)).send(any(), any());
    }


    @Test
    void testManageFileSubmissionVolumes_throw_IOException() throws IOException {
        final String fileName = "test_file.csv";
        List<RecordStatus> recordStatusList = new ArrayList<>();
        recordStatusList.add(new RecordStatus("test", "test", "test", 10));

        when(recordStatusService.findRecordStatus(any(), any())).thenReturn(recordStatusList);
        when(writeFileSubmissionVolumesService.writeFile(any(), any(), any())).thenThrow(IOException.class);
        when(fileNameService.getFileName(any(), any())).thenReturn(fileName);

        File resultFile = useCaseSubmissionVolumes.execute(ItemCommandMock.builderItemCommand(), mockHeaders);

        assertNull(resultFile);
        verify(stateService, times(1)).setError(any());
        verify(producerItemService, times(0)).send(any(), any());
    }

}
