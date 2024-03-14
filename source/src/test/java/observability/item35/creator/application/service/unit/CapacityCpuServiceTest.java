package observability.item35.creator.application.service.unit;

import com.sixgroup.refit.observability.item35.creator.application.service.CapacityCpuServiceImpl;
import com.sixgroup.refit.observability.item35.creator.domain.model.Capacity;
import com.sixgroup.refit.observability.item35.creator.domain.repository.CapacityCpuRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class CapacityCpuServiceTest {

    @Mock
    private CapacityCpuRepository capacityCpuRepository;

    @InjectMocks
    private CapacityCpuServiceImpl capacityCpuService;


    @Test
    void findByCapacityCpu_WithValidData_ShouldReturnCapacityList() {
        // Mock repository response
        List<Capacity> mockCapacityList = Arrays.asList(
            new Capacity("2024-01-01", "0.8", "0.2", "0.5", "CPU"),
            new Capacity("2024-01-02", "0.9", "0.3", "0.6", "CPU")
        );
        when(capacityCpuRepository.findByCapacityCpu(any(), any())).thenReturn(mockCapacityList);
        List<Capacity> result = capacityCpuService.findByCapacityCpu("20240101");

        assertEquals(2, result.size());
        Capacity capacity1 = result.get(0);
        assertEquals("2024-01-01", capacity1.getDate());
        assertEquals("0.002", capacity1.getMin());
        assertEquals("0.008", capacity1.getMax());
        assertEquals("0.005", capacity1.getMean());

        Capacity capacity2 = result.get(1);
        assertEquals("2024-01-02", capacity2.getDate());
        assertEquals("0.003", capacity2.getMin());
        assertEquals("0.009", capacity2.getMax());
        assertEquals("0.006", capacity2.getMean());

    }

    @Test
    void findByCapacityCpu_WithEmptyResult_ShouldReturnEmptyList() {
        when(capacityCpuRepository.findByCapacityCpu(any(), any())).thenReturn(null);
        List<Capacity> result = capacityCpuService.findByCapacityCpu("2024-01-01");
        assertNotNull(result);
        assertTrue(result.isEmpty());

    }
}
