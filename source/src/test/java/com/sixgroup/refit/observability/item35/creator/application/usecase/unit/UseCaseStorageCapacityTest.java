package com.sixgroup.refit.observability.item35.creator.application.usecase.unit;

import com.sixgroup.refit.observability.item.state.application.StateService;
import com.sixgroup.refit.observability.item.state.domain.model.StateRequest;
import com.sixgroup.refit.observability.item35.creator.application.service.FileNameService;
import com.sixgroup.refit.observability.item35.creator.application.service.StorageService;
import com.sixgroup.refit.observability.item35.creator.application.usecase.UseCaseStorageCapacity;
import com.sixgroup.refit.observability.item35.creator.configuration.ReportItemProperties;
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
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.List;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.AppConstants.NUM_DECIMALS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UseCaseStorageCapacityTest {

    private final Headers mockHeaders = mock(Headers.class);

    @InjectMocks
    private UseCaseStorageCapacity useCaseStorageCapacity;

    @Mock
    private WriteFileItem35Service<StorageCapacityDto> writeFileStorageCapacityService;

    @Mock
    private ProducerItemService producerItemService;

    @Mock
    private StorageService storageService;

    @Mock
    private FileNameService fileNameService;

    @Mock
    private StateService stateService;

    @Mock
    private ReportItemProperties reportItemProperties;

    @Captor
    ArgumentCaptor<Iterator<StorageCapacityDto>> storageCapacityDtoIteratorCaptor;

    private static ItemCommandDTO getItemCommandDTO() {
        return ItemCommandDTO.builder()
            .itemId("item35")
            .itemDate("20240915")
            .itemType(ItemType.STORAGE_CAPACITY.getName())
            .command(Command.REQUEST.getDescription())
            .build();
    }

    private BigDecimal bigDecimalFromString(final String value) {
        return BigDecimal.valueOf(Float.parseFloat(value));
    }

    @Test
    void execute_ok() throws IOException {
        String fileName = "test_file.csv";

        List<Storage> totalCapacityList = List.of(
            new Storage("2023-09-01T00:00:00.000Z", bigDecimalFromString("16.703632")),
            new Storage("2023-09-02T00:00:00.000Z", bigDecimalFromString("16.700466"))
        );

        List<Storage> totalFreeCapacityList = List.of(
            new Storage("2023-09-01T00:00:00.000Z", bigDecimalFromString("16.464956")),
            new Storage("2023-09-02T00:00:00.000Z", bigDecimalFromString("16.486961"))
        );

        when(storageService.getTotalCapacity(any(), any())).thenReturn(totalCapacityList);
        when(storageService.getTotalFreeCapacity(any(), any())).thenReturn(totalFreeCapacityList);
        when(fileNameService.getFileName(any(), any())).thenReturn(fileName);
        when(writeFileStorageCapacityService.writeFileStreaming(any(), any(), any())).thenReturn(new File(fileName));

        File result = useCaseStorageCapacity.execute(getItemCommandDTO(), mockHeaders);

        assertNotNull(result);
        assertEquals(fileName, result.getName());
        verify(producerItemService).send(any(), any());
    }

    @Test
    void execute_when_total_capacity_null_then_set_error_and_no_send() {
        when(fileNameService.getFileName(any(), any())).thenReturn("test_file.csv");
        when(storageService.getTotalCapacity(any(), any())).thenReturn(null);
        when(storageService.getTotalFreeCapacity(any(), any())).thenReturn(List.of(
            new Storage("2023-09-01T00:00:00.000Z", bigDecimalFromString("1"))
        ));

        File result = useCaseStorageCapacity.execute(getItemCommandDTO(), mockHeaders);

        assertNull(result);
        verify(stateService, times(1)).setError(any(StateRequest.class));
        verify(producerItemService, never()).send(any(), any());
    }

    @Test
    void execute_when_total_free_capacity_null_then_set_error_and_no_send() {
        when(fileNameService.getFileName(any(), any())).thenReturn("test_file.csv");
        when(storageService.getTotalCapacity(any(), any())).thenReturn(List.of(
            new Storage("2023-09-01T00:00:00.000Z", bigDecimalFromString("1"))
        ));
        when(storageService.getTotalFreeCapacity(any(), any())).thenReturn(null);

        File result = useCaseStorageCapacity.execute(getItemCommandDTO(), mockHeaders);

        assertNull(result);
        verify(stateService, times(1)).setError(any(StateRequest.class));
        verify(producerItemService, never()).send(any(), any());
    }

    @Test
    void execute_when_write_file_throws_then_set_error_and_no_send() throws IOException {
        when(fileNameService.getFileName(any(), any())).thenReturn("test_file.csv");

        List<Storage> totalCapacityList = List.of(
            new Storage("2023-09-01T00:00:00.000Z", bigDecimalFromString("10"))
        );
        List<Storage> totalFreeCapacityList = List.of(
            new Storage("2023-09-01T00:00:00.000Z", bigDecimalFromString("5"))
        );

        when(storageService.getTotalCapacity(any(), any())).thenReturn(totalCapacityList);
        when(storageService.getTotalFreeCapacity(any(), any())).thenReturn(totalFreeCapacityList);

        when(writeFileStorageCapacityService.writeFileStreaming(any(), any(), any()))
            .thenThrow(new IOException("disk error"));

        File result = useCaseStorageCapacity.execute(getItemCommandDTO(), mockHeaders);

        assertNull(result);
        verify(stateService, times(1)).setError(any(StateRequest.class));
        verify(producerItemService, never()).send(any(), any());
    }

    @Test
    void execute_when_write_file_returns_null_then_set_error_and_no_send() throws IOException {
        when(fileNameService.getFileName(any(), any())).thenReturn("test_file.csv");

        List<Storage> totalCapacityList = List.of(
            new Storage("2023-09-01T00:00:00.000Z", bigDecimalFromString("10"))
        );
        List<Storage> totalFreeCapacityList = List.of(
            new Storage("2023-09-01T00:00:00.000Z", bigDecimalFromString("5"))
        );

        when(storageService.getTotalCapacity(any(), any())).thenReturn(totalCapacityList);
        when(storageService.getTotalFreeCapacity(any(), any())).thenReturn(totalFreeCapacityList);
        when(writeFileStorageCapacityService.writeFileStreaming(any(), any(), any())).thenReturn(null);

        File result = useCaseStorageCapacity.execute(getItemCommandDTO(), mockHeaders);

        assertNull(result);
        verify(stateService, times(1)).setError(any(StateRequest.class));
        verify(producerItemService, never()).send(any(), any());
    }

    // FIX 3: capacidad 0 no provoca ArithmeticException (utilization = 0)
    @Test
    void execute_when_free_capacity_zero_then_no_division_by_zero_and_success() throws IOException {
        when(fileNameService.getFileName(any(), any())).thenReturn("test_file.csv");

        List<Storage> totalCapacityList = List.of(
            new Storage("2023-09-01T00:00:00.000Z", bigDecimalFromString("10"))
        );
        List<Storage> totalFreeCapacityList = List.of(
            new Storage("2023-09-01T00:00:00.000Z", BigDecimal.ZERO)
        );

        when(storageService.getTotalCapacity(any(), any())).thenReturn(totalCapacityList);
        when(storageService.getTotalFreeCapacity(any(), any())).thenReturn(totalFreeCapacityList);
        when(writeFileStorageCapacityService.writeFileStreaming(any(), any(), any()))
            .thenReturn(new File("test_file.csv"));

        File result = useCaseStorageCapacity.execute(getItemCommandDTO(), mockHeaders);

        assertNotNull(result);
        verify(writeFileStorageCapacityService).writeFileStreaming(
            storageCapacityDtoIteratorCaptor.capture(), any(), any());

        Iterator<StorageCapacityDto> generated = storageCapacityDtoIteratorCaptor.getValue();
        assertTrue(generated.hasNext());

        assertEquals(
            BigDecimal.ONE.setScale(NUM_DECIMALS, RoundingMode.HALF_UP),
            generated.next().getUtilization()
        );
        assertFalse(generated.hasNext());

        verify(producerItemService, times(1)).send(any(), any());
        verify(stateService, never()).setError(any());
    }

    @Test
    void getItemType() {
        assertEquals(ItemType.STORAGE_CAPACITY, useCaseStorageCapacity.getItemType());
    }
}
