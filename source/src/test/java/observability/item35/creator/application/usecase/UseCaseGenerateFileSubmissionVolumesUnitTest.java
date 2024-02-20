package observability.item35.creator.application.usecase;

import com.sixgroup.refit.observability.item35.creator.application.usecase.UseCaseGenerateFileSubmissionVolumes;
import com.sixgroup.refit.observability.item35.creator.configuration.CsvProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.RecordStatus;
import com.sixgroup.refit.observability.item35.creator.domain.service.ItemReportingService;
import com.sixgroup.refit.observability.item35.creator.domain.service.ProducerItemService;
import com.sixgroup.refit.observability.item35.creator.domain.service.RecordStatusService;
import com.sixgroup.refit.observability.item35.creator.domain.service.WriteFileItem35Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UseCaseGenerateFileSubmissionVolumesUnitTest {

    @Mock
    private RecordStatusService recordStatusService;

    @Mock
    private WriteFileItem35Service writeFileSubmissionVolumesService;

    @Mock
    private ItemReportingService itemReportingService;

    @Mock
    private ProducerItemService producerItemService;

    @Mock
    private CsvProperties csvProperties;

    @InjectMocks
    private UseCaseGenerateFileSubmissionVolumes useCaseGenerateFileSubmissionVolumes;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testManageFileSubmissionVolumes_Success() throws Exception {
        List<RecordStatus> recordStatusList = new ArrayList<>();
        recordStatusList.add(new RecordStatus("test","test","test",10));
        when(recordStatusService.findRecordStatus()).thenReturn(recordStatusList);
        File mockedFile = new File("test_file.csv");
        when(writeFileSubmissionVolumesService.writeFile(anyList(), anyString())).thenReturn(mockedFile);
        File resultFile = useCaseGenerateFileSubmissionVolumes.manageFileSubmissionVolumes();
        assertNotNull(resultFile);
        verify(itemReportingService, times(1)).insertItemReporting(any());
        verify(producerItemService, times(1)).send(any(), any());
    }

    @Test
    public void testManageFileSubmissionVolumes_NoRecordStatusFound(){
        when(recordStatusService.findRecordStatus()).thenReturn(new ArrayList<>());
        File resultFile = useCaseGenerateFileSubmissionVolumes.manageFileSubmissionVolumes();
        assertNull(resultFile);
        verify(itemReportingService, times(1)).insertItemReporting(any());
        verify(producerItemService, times(0)).send(any(), any());
    }


    @Test
    public void testManageFileSubmissionVolumes_throw_IOException() throws IOException {
        when(recordStatusService.findRecordStatus()).thenReturn(new ArrayList<>());
        List<RecordStatus> recordStatusList = new ArrayList<>();
        recordStatusList.add(new RecordStatus("test","test","test",10));
        when(writeFileSubmissionVolumesService.writeFile(any(),any())).thenThrow(IOException.class);
        File resultFile = useCaseGenerateFileSubmissionVolumes.manageFileSubmissionVolumes();
        assertNull(resultFile);
        verify(itemReportingService, times(1)).insertItemReporting(any());
        verify(producerItemService, times(0)).send(any(), any());
    }

}
