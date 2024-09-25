package com.sixgroup.refit.observability.item35.creator.application.usecase.unit;

import com.sixgroup.refit.observability.item.state.application.StateService;
import com.sixgroup.refit.observability.item.state.domain.model.StateRequest;
import com.sixgroup.refit.observability.item35.creator.application.service.StorageService;
import com.sixgroup.refit.observability.item35.creator.application.usecase.UseCaseStorageCapacity;
import com.sixgroup.refit.observability.item35.creator.domain.enums.Command;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.model.Storage;
import com.sixgroup.refit.observability.item35.creator.domain.model.StorageCapacityDto;
import com.sixgroup.refit.observability.item35.creator.domain.service.ProducerItemService;
import com.sixgroup.refit.observability.item35.creator.domain.service.WriteFileItem35Service;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UseCaseStorageCapacityTest {

    @Captor
    ArgumentCaptor<List<StorageCapacityDto>> storageCapacityDtoListCaptor;
    @Mock
    private WriteFileItem35Service<StorageCapacityDto> writeFileSubmissionVolumesService;
    @Mock
    private ProducerItemService producerItemService;
    @Mock
    private StateService stateService;
    @InjectMocks
    private UseCaseStorageCapacity useCaseStorageCapacity;
    @Mock
    private StorageService storageService;
    private final Headers mockHeaders = mock(Headers.class);

    private static StorageCapacityDto createStorageCapacity(String reportDay, String date, String timeStamp, float capacity,
                                                            float usedCapacity, float availableCapacity, float utilization) {
        StorageCapacityDto storageCapacityDto = new StorageCapacityDto();
        storageCapacityDto.setReportingDate(reportDay);
        storageCapacityDto.setDate(date);
        storageCapacityDto.setTimeStamp(timeStamp);
        storageCapacityDto.setCapacity(capacity);
        storageCapacityDto.setUsedCapacity(usedCapacity);
        storageCapacityDto.setAvailableCapacity(availableCapacity);
        storageCapacityDto.setUtilization(utilization);
        return storageCapacityDto;
    }

    private static ItemCommandDTO getItemCommandDTO() {
        return ItemCommandDTO.builder()
            .itemDate("20240915")
            .itemType(ItemType.STORAGE_CAPACITY.getName())
            .command(Command.REQUEST.getDescription())
            .build();
    }

    @Test
    void execute() throws IOException {
        List<Storage> totalCapacityList = List.of(new Storage("2023-09-01T00:00:00.000Z", 16.703632f),
            new Storage("2023-09-02T00:00:00.000Z", 16.700466f));
        when(storageService.getTotalCapacity(any(), any())).thenReturn(totalCapacityList);

        List<Storage> totalFreeCapacityList = List.of(new Storage("2023-09-01T00:00:00.000Z", 16.464956f),
            new Storage("2023-09-02T00:00:00.000Z", 16.486961f));
        when(storageService.getTotalFreeCapacity(any(), any())).thenReturn(totalFreeCapacityList);

        StorageCapacityDto storageCapacityDto_1 = createStorageCapacity("2024-09-15", "2023-09-01",
            "2023-09-01T00:00:00.000Z", 16.7036f, 0.2386f, 16.4650f,
            0.0143f);

        StorageCapacityDto storageCapacityDto_2 = createStorageCapacity("2024-09-15", "2023-09-02",
            "2023-09-02T00:00:00.000Z", 16.7036f, 0.2166f, 16.4870f,
            0.013f);

        List<StorageCapacityDto> storageCapacityDtoList = List.of(storageCapacityDto_1, storageCapacityDto_2);

        File mockedFile = new File("test_file.csv");
        when(writeFileSubmissionVolumesService.writeFile(anyList(), any())).thenReturn(mockedFile);

        ItemCommandDTO itemCommandDTO = getItemCommandDTO();

        File resultFile = useCaseStorageCapacity.execute(itemCommandDTO, mockHeaders);
        assertNotNull(resultFile);
        verify(storageService, times(1)).getTotalCapacity(any(), any());
        verify(storageService, times(1)).getTotalFreeCapacity(any(), any());
        verify(stateService, times(4)).nextStep(any(StateRequest.class));
        verify(producerItemService, times(1)).send(any(), any());

        verify(writeFileSubmissionVolumesService).writeFile(storageCapacityDtoListCaptor.capture(),
            any());
        List<StorageCapacityDto> value = storageCapacityDtoListCaptor.getValue();
        assertEquals(value, storageCapacityDtoList);
    }

    @Test
    void execute_resource_not_found_total_capacity_error() {
        when(storageService.getTotalCapacity(any(), any())).thenReturn(null);
        useCaseStorageCapacity.execute(getItemCommandDTO(), mockHeaders);
        verify(stateService, times(1)).setError(any());
        verify(producerItemService, times(0)).send(any(), any());
        verify(storageService, times(1)).getTotalCapacity(any(), any());
        verify(storageService, times(1)).getTotalFreeCapacity(any(), any());
    }

    @Test
    void execute_resource_not_found_total_free_capacity_error() {
        List<Storage> totalCapacityList = List.of(new Storage("2023-09-01T00:00:00.000Z", 16.703632f),
            new Storage("2023-09-02T00:00:00.000Z", 16.700466f));
        when(storageService.getTotalCapacity(any(), any())).thenReturn(totalCapacityList);
        when(storageService.getTotalFreeCapacity(any(), any())).thenReturn(null);
        useCaseStorageCapacity.execute(getItemCommandDTO(), mockHeaders);
        verify(stateService, times(1)).setError(any());
        verify(producerItemService, times(0)).send(any(), any());
        verify(storageService, times(1)).getTotalCapacity(any(), any());
        verify(storageService, times(1)).getTotalFreeCapacity(any(), any());
    }

    @Test
    void execute_throws_io_exception() throws IOException {
        List<Storage> totalCapacityList = List.of(new Storage("2023-09-01T00:00:00.000Z", 16.703632f),
            new Storage("2023-09-02T00:00:00.000Z", 16.700466f));
        when(storageService.getTotalCapacity(any(), any())).thenReturn(totalCapacityList);

        List<Storage> totalFreeCapacityList = List.of(new Storage("2023-09-01T00:00:00.000Z", 16.464956f),
            new Storage("2023-09-02T00:00:00.000Z", 16.486961f));
        when(storageService.getTotalFreeCapacity(any(), any())).thenReturn(totalFreeCapacityList);

        StorageCapacityDto storageCapacityDto_1 = createStorageCapacity("2024-09-15", "2023-09-01",
            "2023-09-01T00:00:00.000Z", 16.7036f, 0.2386f, 16.4650f,
            0.0143f);

        StorageCapacityDto storageCapacityDto_2 = createStorageCapacity("2024-09-15", "2023-09-02",
            "2023-09-02T00:00:00.000Z", 16.7036f, 0.2166f, 16.4870f,
            0.013f);

        List<StorageCapacityDto> storageCapacityDtoList = List.of(storageCapacityDto_1, storageCapacityDto_2);

        when(writeFileSubmissionVolumesService.writeFile(anyList(), any())).thenThrow(new IOException("Error"));
        useCaseStorageCapacity.execute(getItemCommandDTO(), mockHeaders);
        verify(storageService, times(1)).getTotalCapacity(any(), any());
        verify(storageService, times(1)).getTotalFreeCapacity(any(), any());
        verify(stateService, times(2)).nextStep(any(StateRequest.class));
        verify(producerItemService, times(0)).send(any(), any());

        verify(writeFileSubmissionVolumesService).writeFile(storageCapacityDtoListCaptor.capture(),
            any());
        List<StorageCapacityDto> value = storageCapacityDtoListCaptor.getValue();
        assertEquals(value, storageCapacityDtoList);
    }

    @Test
    void getItemType() {
        assertEquals(ItemType.STORAGE_CAPACITY, useCaseStorageCapacity.getItemType());
    }

}
