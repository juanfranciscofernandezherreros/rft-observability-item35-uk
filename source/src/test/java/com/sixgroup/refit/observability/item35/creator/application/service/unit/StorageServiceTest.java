package com.sixgroup.refit.observability.item35.creator.application.service.unit;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixgroup.refit.observability.item35.creator.application.service.StorageServiceImpl;
import com.sixgroup.refit.observability.item35.creator.configuration.ComponentProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.Storage;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.*;
import com.sixgroup.refit.observability.item35.creator.infrastructure.repository.cloudera.StorageCapacity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.NODE_STORAGE_TOTAL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @InjectMocks
    private StorageServiceImpl storageService;
    @Mock
    private StorageCapacity storageCapacity;

    @Mock
    private ComponentProperties componentProperties;

    ObjectMapper objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Test
    void getTotalCapacity() throws IOException {

        String responseTotalAll =
            new String(Files.readAllBytes(Paths.get("src/test/resources/json/total_all.json")));

        StorageCapacityResponse storageCapacityResponse = objectMapper.readValue(responseTotalAll, StorageCapacityResponse.class);

        when(storageCapacity.findTotalStorage(anyString(), anyString()))
            .thenReturn(storageCapacityResponse);

        ComponentProperties.Storage mockStorage = mock(ComponentProperties.Storage.class);
        when(componentProperties.getStorage()).thenReturn(mockStorage);
        when(mockStorage.getEntityName()).thenReturn("rftemir-cldr-qa-mbt");
        List<Storage> storageList = storageService.getTotalCapacity("2024-01-01", "2024-02-01");

        assertNotNull(storageList);
        assertEquals("2024-01-01T00:00:00.000Z", storageList.get(0).getTimeStamp());
        assertEquals("2024-02-01T00:00:00.000Z", storageList.get(storageList.size() - 1).getTimeStamp());
        assertEquals(17.1004f, storageList.get(0).getCapacity());
        assertEquals(17.1903f, storageList.get(storageList.size() - 1).getCapacity());
        verify(storageCapacity).findTotalStorage(eq("2024-01-01"), eq("2024-02-01"));
    }

    @Test
    void getTotalCapacity_response_cloudera_null() {

        when(storageCapacity.findTotalStorage(anyString(), anyString()))
            .thenReturn(null);

        RuntimeException runtimeException = assertThrows(RuntimeException.class,
            () -> storageService.getTotalCapacity("2024-01-01", "2024-02-01"));

        assertEquals(runtimeException.getMessage(),
            "'apiCloudera findTotalStorage()' response cannot be null");

        verify(storageCapacity).findTotalStorage(eq("2024-01-01"), eq("2024-02-01"));
    }

    @Test
    void getTotalCapacity_response_cloudera_empty() {

        when(storageCapacity.findTotalStorage(anyString(), anyString()))
            .thenReturn(new StorageCapacityResponse());

        RuntimeException runtimeException = assertThrows(RuntimeException.class,
            () -> storageService.getTotalCapacity("2024-01-01", "2024-02-01"));

        assertEquals(runtimeException.getMessage(),
            "Empty list from Cloudera Api 'TotalCapacity' filter");

        verify(storageCapacity).findTotalStorage(eq("2024-01-01"), eq("2024-02-01"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void getTotalCapacity_response_cloudera_item_null_or_empty(List<Items> items) {

        StorageCapacityResponse storageCapacityResponse = new StorageCapacityResponse();
        storageCapacityResponse.setItems(items);

        when(storageCapacity.findTotalStorage(anyString(), anyString()))
            .thenReturn(storageCapacityResponse);

        RuntimeException runtimeException = assertThrows(RuntimeException.class,
            () -> storageService.getTotalCapacity("2024-01-01", "2024-02-01"));

        assertEquals(runtimeException.getMessage(),
            "Empty list from Cloudera Api 'TotalCapacity' filter");

        verify(storageCapacity).findTotalStorage(eq("2024-01-01"), eq("2024-02-01"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void getTotalCapacity_response_cloudera_time_series_null_or_empty(List<TimeSeries> timeSeries) {

        StorageCapacityResponse storageCapacityResponse = new StorageCapacityResponse();
        Items items = new Items();
        items.setTimeSeries(timeSeries);
        storageCapacityResponse.setItems(List.of(items));

        when(storageCapacity.findTotalStorage(anyString(), anyString()))
            .thenReturn(storageCapacityResponse);

        RuntimeException runtimeException = assertThrows(RuntimeException.class,
            () -> storageService.getTotalCapacity("2024-01-01", "2024-02-01"));

        assertEquals(runtimeException.getMessage(),
            "Empty list from Cloudera Api 'TotalCapacity' filter");

        verify(storageCapacity).findTotalStorage(eq("2024-01-01"), eq("2024-02-01"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void getTotalCapacity_response_cloudera_no_data_in_node(List<Data> data) {

        StorageCapacityResponse storageCapacityResponse = new StorageCapacityResponse();
        Items items = new Items();
        TimeSeries series = new TimeSeries();
        Metadata metadata = new Metadata();
        Attributes attributes = new Attributes();
        attributes.setClusterName(NODE_STORAGE_TOTAL);
        metadata.setAttributes(attributes);
        series.setMetadata(metadata);
        series.setData(data);
        items.setTimeSeries(List.of(series));
        storageCapacityResponse.setItems(List.of(items));

        when(storageCapacity.findTotalStorage(anyString(), anyString()))
            .thenReturn(storageCapacityResponse);

        RuntimeException runtimeException = assertThrows(RuntimeException.class,
            () -> storageService.getTotalCapacity("2024-01-01", "2024-02-01"));

        assertEquals(runtimeException.getMessage(),
            "Empty list from Cloudera Api 'TotalCapacity' filter");

        verify(storageCapacity).findTotalStorage(eq("2024-01-01"), eq("2024-02-01"));
    }

    @Test
    void getTotalFreeCapacity() throws IOException {

        String responseFreeAll =
            new String(Files.readAllBytes(Paths.get("src/test/resources/json/free_all.json")));

        StorageCapacityResponse storageCapacityResponse = objectMapper.readValue(responseFreeAll, StorageCapacityResponse.class);

        when(storageCapacity.findFreeStorage(anyString(), anyString()))
            .thenReturn(storageCapacityResponse);

        List<Storage> storageList = storageService.getTotalFreeCapacity("2024-01-01", "2024-02-01");

        assertNotNull(storageList);

        assertNotNull(storageList);
        assertEquals("2024-01-01T00:00:00.000Z", storageList.get(0).getTimeStamp());
        assertEquals("2024-02-01T00:00:00.000Z", storageList.get(storageList.size() - 1).getTimeStamp());
        assertEquals(16.3134f, storageList.get(0).getCapacity());
        assertEquals(15.9577f, storageList.get(storageList.size() - 1).getCapacity());

        verify(storageCapacity).findFreeStorage(eq("2024-01-01"), eq("2024-02-01"));
    }

    @Test
    void getTotalFreeCapacity_response_cloudera_null() {

        when(storageCapacity.findFreeStorage(anyString(), anyString()))
            .thenReturn(null);

        RuntimeException runtimeException = assertThrows(RuntimeException.class,
            () -> storageService.getTotalFreeCapacity("2024-01-01", "2024-02-01"));

        assertEquals(runtimeException.getMessage(),
            "'apiCloudera findFreeStorage()' response cannot be null");

        verify(storageCapacity).findFreeStorage(eq("2024-01-01"), eq("2024-02-01"));
    }

    @Test
    void getTotalFreeCapacity_response_cloudera_empty() {

        when(storageCapacity.findFreeStorage(anyString(), anyString()))
            .thenReturn(new StorageCapacityResponse());

        RuntimeException runtimeException = assertThrows(RuntimeException.class,
            () -> storageService.getTotalFreeCapacity("2024-01-01", "2024-02-01"));

        assertEquals(runtimeException.getMessage(),
            "Empty list from Cloudera Api 'TotalFreeCapacity' filter");

        verify(storageCapacity).findFreeStorage(eq("2024-01-01"), eq("2024-02-01"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void getTotalFreeCapacity_response_cloudera_item_null_or_empty(List<Items> items) {

        StorageCapacityResponse storageCapacityResponse = new StorageCapacityResponse();
        storageCapacityResponse.setItems(items);

        when(storageCapacity.findFreeStorage(anyString(), anyString()))
            .thenReturn(storageCapacityResponse);

        RuntimeException runtimeException = assertThrows(RuntimeException.class,
            () -> storageService.getTotalFreeCapacity("2024-01-01", "2024-02-01"));

        assertEquals(runtimeException.getMessage(),
            "Empty list from Cloudera Api 'TotalFreeCapacity' filter");

        verify(storageCapacity).findFreeStorage(eq("2024-01-01"), eq("2024-02-01"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void getTotalFreeCapacity_response_cloudera_time_series_null_or_empty(List<TimeSeries> timeSeries) {

        StorageCapacityResponse storageCapacityResponse = new StorageCapacityResponse();
        Items items = new Items();
        items.setTimeSeries(timeSeries);
        storageCapacityResponse.setItems(List.of(items));

        when(storageCapacity.findFreeStorage(anyString(), anyString()))
            .thenReturn(storageCapacityResponse);

        RuntimeException runtimeException = assertThrows(RuntimeException.class,
            () -> storageService.getTotalFreeCapacity("2024-01-01", "2024-02-01"));

        assertEquals(runtimeException.getMessage(),
            "Empty list from Cloudera Api 'TotalFreeCapacity' filter");

        verify(storageCapacity).findFreeStorage(eq("2024-01-01"), eq("2024-02-01"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void getTotalFreeCapacity_response_cloudera_no_data_in_node(List<Data> data) {

        StorageCapacityResponse storageCapacityResponse = new StorageCapacityResponse();
        Items items = new Items();
        TimeSeries series = new TimeSeries();
        Metadata metadata = new Metadata();
        Attributes attributes = new Attributes();
        attributes.setClusterName(NODE_STORAGE_TOTAL);
        metadata.setAttributes(attributes);
        series.setMetadata(metadata);
        series.setData(data);
        items.setTimeSeries(List.of(series));
        storageCapacityResponse.setItems(List.of(items));

        when(storageCapacity.findFreeStorage(anyString(), anyString()))
            .thenReturn(storageCapacityResponse);

        RuntimeException runtimeException = assertThrows(RuntimeException.class,
            () -> storageService.getTotalFreeCapacity("2024-01-01", "2024-02-01"));

        assertEquals(runtimeException.getMessage(),
            "Empty list from Cloudera Api 'TotalFreeCapacity' filter");

        verify(storageCapacity).findFreeStorage(eq("2024-01-01"), eq("2024-02-01"));
    }

}
