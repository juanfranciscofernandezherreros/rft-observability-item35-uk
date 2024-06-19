package com.sixgroup.refit.observability.item35.creator.application.usecase.unit;

import com.sixgroup.refit.observability.item.state.application.StateService;
import com.sixgroup.refit.observability.item.state.domain.model.StateRequest;
import com.sixgroup.refit.observability.item35.creator.application.mock.CapacityMock;
import com.sixgroup.refit.observability.item35.creator.application.mock.ItemCommandMock;
import com.sixgroup.refit.observability.item35.creator.application.service.CapacityCpuService;
import com.sixgroup.refit.observability.item35.creator.application.service.CapacityRamService;
import com.sixgroup.refit.observability.item35.creator.application.usecase.UseCaseComputeCapacity;
import com.sixgroup.refit.observability.item35.creator.domain.model.Capacity;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.service.ProducerItemService;
import com.sixgroup.refit.observability.item35.creator.domain.service.WriteFileItem35Service;
import org.apache.kafka.common.header.Headers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UseCaseComputeCapacityUnitTest {

    @Mock
    private CapacityCpuService capacityCpuService;

    @Mock
    private CapacityRamService capacityRamService;

    @Mock
    private WriteFileItem35Service writeFileComputeCapacity;

    @Mock
    private ProducerItemService producerItemService;

    @Mock
    private StateService stateService;

    @InjectMocks
    private UseCaseComputeCapacity useCaseComputeCapacity;

    private final Headers mockHeaders = mock(Headers.class);


    @Test
    void testExecuteSuccess() throws Exception {

        ItemCommandDTO itemCommandDTO = ItemCommandMock.builderItemCommandComputeCapacity();
        List<Capacity> capacitiesCpu = CapacityMock.builderListCapacityCpu();
        List<Capacity> capacitiesRam = CapacityMock.builderListCapacityRam();
        when(capacityCpuService.findByCapacityCpu(anyString())).thenReturn(capacitiesCpu);
        when(capacityRamService.findByCapacityRam(anyString())).thenReturn(capacitiesRam);
        File mockFile = mock(File.class);
        when(writeFileComputeCapacity.writeFile(anyList(), any(ItemCommandDTO.class))).thenReturn(mockFile);
        useCaseComputeCapacity.execute(itemCommandDTO, mockHeaders);
        verify(capacityCpuService, times(1)).findByCapacityCpu(anyString());
        verify(capacityRamService, times(1)).findByCapacityRam(anyString());
        verify(writeFileComputeCapacity, times(1)).writeFile(anyList(), any(ItemCommandDTO.class));
        verify(producerItemService, times(1)).send(any(ItemCommandDTO.class), any());
        verify(stateService, times(1)).nextStep(any(StateRequest.class));
    }

    @Test
    void testExecuteError() throws Exception {

        ItemCommandDTO itemCommandDTO = ItemCommandMock.builderItemCommandComputeCapacity();
        List<Capacity> capacitiesCpu = List.of();
        List<Capacity> capacitiesRam = CapacityMock.builderListCapacityRam();
        when(capacityCpuService.findByCapacityCpu(anyString())).thenReturn(capacitiesCpu);
        when(capacityRamService.findByCapacityRam(anyString())).thenReturn(capacitiesRam);
        useCaseComputeCapacity.execute(itemCommandDTO, mockHeaders);
        verify(capacityCpuService, times(1)).findByCapacityCpu(anyString());
        verify(capacityRamService, times(1)).findByCapacityRam(anyString());
        verify(writeFileComputeCapacity, times(0)).writeFile(anyList(), any(ItemCommandDTO.class));
        verify(producerItemService, times(0)).send(any(ItemCommandDTO.class), any());
        verify(stateService, times(1)).setError(any(StateRequest.class));
    }
}
