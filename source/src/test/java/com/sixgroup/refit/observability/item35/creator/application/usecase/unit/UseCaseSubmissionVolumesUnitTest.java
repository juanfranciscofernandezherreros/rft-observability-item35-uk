package com.sixgroup.refit.observability.item35.creator.application.usecase.unit;

import com.sixgroup.refit.observability.item.state.application.StateService;
import com.sixgroup.refit.observability.item.state.domain.model.StateRequest;
import com.sixgroup.refit.observability.item35.creator.application.mock.ItemCommandMock;
import com.sixgroup.refit.observability.item35.creator.application.usecase.UseCaseSubmissionVolumes;
import com.sixgroup.refit.observability.item35.creator.domain.model.RecordStatus;
import com.sixgroup.refit.observability.item35.creator.domain.service.ProducerItemService;
import com.sixgroup.refit.observability.item35.creator.domain.service.RecordStatusService;
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
    @Mock
    private RecordStatusService recordStatusService;
    @Mock
    private WriteFileItem35Service writeFileSubmissionVolumesService;
    @Mock
    private ProducerItemService producerItemService;
    @Mock
    private StateService stateService;
    @InjectMocks
    private UseCaseSubmissionVolumes useCaseSubmissionVolumes;

    @Test
    void testManageFileSubmissionVolumes_Success() throws Exception {
        List<RecordStatus> recordStatusList = new ArrayList<>();
        recordStatusList.add(new RecordStatus("2024-01-01", "test", "web", 10));
        when(recordStatusService.findRecordStatus(any())).thenReturn(recordStatusList);
        File mockedFile = new File("test_file.csv");
        when(writeFileSubmissionVolumesService.writeFile(anyList(), any())).thenReturn(mockedFile);
        File resultFile = useCaseSubmissionVolumes.execute(ItemCommandMock.builderItemCommand(), mockHeaders);
        assertNotNull(resultFile);
        verify(stateService, times(1)).nextStep((StateRequest) any());
        verify(producerItemService, times(1)).send(any(), any());
    }

    @Test
    void testManageFileSubmissionVolumes_NoRecordStatusFound() {
        when(recordStatusService.findRecordStatus(any())).thenReturn(new ArrayList<>());
        File resultFile = useCaseSubmissionVolumes.execute(ItemCommandMock.builderItemCommand(), mockHeaders);
        assertNull(resultFile);
        verify(stateService, times(1)).setError(any());
        verify(producerItemService, times(0)).send(any(), any());
    }


    @Test
    void testManageFileSubmissionVolumes_throw_IOException() throws IOException {
        List<RecordStatus> recordStatusList = new ArrayList<>();
        recordStatusList.add(new RecordStatus("test", "test", "test", 10));
        when(recordStatusService.findRecordStatus(any())).thenReturn(recordStatusList);
        when(writeFileSubmissionVolumesService.writeFile(any(), any())).thenThrow(IOException.class);
        File resultFile = useCaseSubmissionVolumes.execute(ItemCommandMock.builderItemCommand(), mockHeaders);
        assertNull(resultFile);
        verify(stateService, times(1)).setError(any());
        verify(producerItemService, times(0)).send(any(), any());
    }

}
