package com.sixgroup.refit.observability.item35.creator.application.service.unit;


import com.sixgroup.refit.observability.item35.creator.application.service.CapacityRamServiceImpl;
import com.sixgroup.refit.observability.item35.creator.domain.model.Capacity;
import com.sixgroup.refit.observability.item35.creator.domain.repository.CapacityRamRepository;
import com.sixgroup.refit.observability.item35.creator.shared.constants.CapacityConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.sixgroup.refit.observability.item35.creator.application.mock.CapacityMock.builderListCapacityRam;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class CapacityRamServiceTest {

    @Mock
    private CapacityRamRepository capacityRamRepository;

    @InjectMocks
    private CapacityRamServiceImpl capacityRamService;

    @Test
    void testFindByCapacityRam() {
        when(capacityRamRepository.findByCapacityRam(anyString(), anyString())).thenReturn(builderListCapacityRam());
        List<Capacity> result = capacityRamService.findByCapacityRam("20240101");
        verify(capacityRamRepository).findByCapacityRam(eq("2024-01-01"), eq("2024-02-01"));
        assertNotNull(result);

        Assertions.assertEquals(result,
            List.of(new Capacity("2024-01-01", "0.232", "0.220", "0.224", CapacityConstants.RAM)));
    }

}
