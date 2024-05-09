package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.cloudera;

import com.sixgroup.refit.observability.item35.creator.configuration.ApiClouderaProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.ClouderaProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.StorageCapacityResponse;
import okhttp3.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageCapacityTest {

    @Mock
    private OkHttpClient mockOkHttpClient;

    @Mock
    private ApiClouderaProperties mockApiClouderaProperties;

    @Mock
    private ClouderaProperties mockClouderaProperties;

    @Mock
    private Call mockCall;

    @InjectMocks
    private StorageCapacity storageCapacity;

    @Test
    void testfindTotalStorage() throws IOException {
        // Prepare test data and mocks
        String testDateFrom = "2020-01-01";
        String testDateTo = "2020-02-01";
        String testResponseBody = "{\"data\": [{\"value\": 100, \"date\": \"2020-01-01\"}]}";
        Response mockResponse = new Response.Builder()
            .request(new Request.Builder().url("http://test").build())
            .protocol(Protocol.HTTP_2)
            .code(200)
            .message("OK")
            .body(ResponseBody.create(MediaType.get("application/json"), testResponseBody))
            .build();

        when(mockOkHttpClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);
        when(mockApiClouderaProperties.getHost()).thenReturn("http://localhost");
        when(mockApiClouderaProperties.getPort()).thenReturn("8080");
        when(mockApiClouderaProperties.getUrl()).thenReturn("/api/test");
        ClouderaProperties.Storage mockStorage = mock(ClouderaProperties.Storage.class);
        when(mockClouderaProperties.getStorage()).thenReturn(mockStorage);
        when(mockStorage.getSelectTotalApi()).thenReturn("select total_capacity_across_filesystems");

        // Execute the method under test
        StorageCapacityResponse result =
            storageCapacity.findTotalStorage(testDateFrom, testDateTo);

        // Verify results
        assertNotNull(result);
        verify(mockOkHttpClient).newCall(any(Request.class));
    }

    @Test
    void testfindTotalFreeStorage() throws IOException {
        // Prepare test data and mocks
        String testDateFrom = "2020-01-01";
        String testDateTo = "2020-02-01";
        String testResponseBody = "{\"data\": [{\"value\": 100, \"date\": \"2020-01-01\"}]}";
        Response mockResponse = new Response.Builder()
            .request(new Request.Builder().url("http://test").build())
            .protocol(Protocol.HTTP_2)
            .code(200)
            .message("OK")
            .body(ResponseBody.create(MediaType.get("application/json"), testResponseBody))
            .build();

        when(mockOkHttpClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);
        when(mockApiClouderaProperties.getHost()).thenReturn("http://localhost");
        when(mockApiClouderaProperties.getPort()).thenReturn("8080");
        when(mockApiClouderaProperties.getUrl()).thenReturn("/api/test");
        ClouderaProperties.Storage mockStorage = mock(ClouderaProperties.Storage.class);
        when(mockClouderaProperties.getStorage()).thenReturn(mockStorage);
        when(mockStorage.getSelectFreeApi()).thenReturn("select total_capacity_free_across_filesystems");

        // Execute the method under test
        StorageCapacityResponse storageCapacityResponse =
            storageCapacity.findFreeStorage(testDateFrom, testDateTo);

        // Verify results
        assertNotNull(storageCapacityResponse);
        verify(mockOkHttpClient).newCall(any(Request.class));
    }

    @Test
    void call_cloudera_total_error() throws IOException {
        // Prepare test data and mocks
        String testDateFrom = "2020-01-01";
        String testDateTo = "2020-02-01";

        when(mockOkHttpClient.newCall(any(Request.class))).thenThrow(new RuntimeException());
        when(mockApiClouderaProperties.getHost()).thenReturn("http://localhost");
        when(mockApiClouderaProperties.getPort()).thenReturn("8080");
        when(mockApiClouderaProperties.getUrl()).thenReturn("/api/test");
        ClouderaProperties.Storage mockStorage = mock(ClouderaProperties.Storage.class);
        when(mockClouderaProperties.getStorage()).thenReturn(mockStorage);
        when(mockStorage.getSelectTotalApi()).thenReturn("select total_capacity_across_filesystems");

        StorageCapacityResponse storageCapacityResponse = storageCapacity.findTotalStorage(testDateFrom, testDateTo);

        // Verify results
        assertNull(storageCapacityResponse);
        verify(mockOkHttpClient).newCall(any(Request.class));
    }

    @Test
    void call_cloudera_free_total_error() throws IOException {
        // Prepare test data and mocks
        String testDateFrom = "2020-01-01";
        String testDateTo = "2020-02-01";

        when(mockOkHttpClient.newCall(any(Request.class))).thenThrow(new RuntimeException());
        when(mockApiClouderaProperties.getHost()).thenReturn("http://localhost");
        when(mockApiClouderaProperties.getPort()).thenReturn("8080");
        when(mockApiClouderaProperties.getUrl()).thenReturn("/api/test");
        ClouderaProperties.Storage mockStorage = mock(ClouderaProperties.Storage.class);
        when(mockClouderaProperties.getStorage()).thenReturn(mockStorage);
        when(mockStorage.getSelectFreeApi()).thenReturn("select total_capacity_free_across_filesystems");
        StorageCapacityResponse storageCapacityResponse = storageCapacity.findFreeStorage(testDateFrom, testDateTo);
        Assertions.assertNull(storageCapacityResponse);
        verify(mockOkHttpClient).newCall(any(Request.class));
    }

}
