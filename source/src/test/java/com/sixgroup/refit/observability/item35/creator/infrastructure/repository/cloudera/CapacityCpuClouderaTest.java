package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.cloudera;

import com.sixgroup.refit.observability.item35.creator.configuration.ApiClouderaProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.ComponentProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.Capacity;
import okhttp3.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CapacityCpuClouderaTest {

    @Mock
    private OkHttpClient mockOkHttpClient;

    @Mock
    private ApiClouderaProperties mockApiClouderaProperties;

    @Mock
    private ComponentProperties mockComponentProperties;

    @Mock
    private Call mockCall;

    @InjectMocks
    private CapacityCpuCloudera capacityCpuCloudera;

    @Test
    void testFindByCapacityCpuSuccessful() throws IOException {

        String testDateFrom = "2020-01-01";
        String testDateTo = "2020-01-31";
        String testResponseBody = "{\"data\": [{\"value\": 100, \"date\": \"2020-01-01\"}]}";
        Response mockResponse = new Response.Builder()
            .request(new Request.Builder().url("http://test").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(ResponseBody.create(MediaType.get("application/json"), testResponseBody))
            .build();

        when(mockOkHttpClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);
        when(mockApiClouderaProperties.getHost()).thenReturn("https://localhost");
        when(mockApiClouderaProperties.getPort()).thenReturn("8080");
        when(mockApiClouderaProperties.getUrl()).thenReturn("/api/test");
        ComponentProperties.Cpu mockCpu = mock(ComponentProperties.Cpu.class);
        when(mockComponentProperties.getCpu()).thenReturn(mockCpu);
        when(mockCpu.getSelectCpu()).thenReturn("select * from test");
        when(mockCpu.getDesiredRollup()).thenReturn("DAILY");
        List<Capacity> result = capacityCpuCloudera.findByCapacityCpu(testDateFrom, testDateTo);
        assertNotNull(result);
        verify(mockOkHttpClient).newCall(any(Request.class));
    }

    @Test
    void testFindByCapacityCPUFailure() throws IOException {

        String dateFrom = "2020-01-01";
        String dateTo = "2020-01-02";

        HttpUrl url = new HttpUrl.Builder()
            .scheme("http")
            .host("example.com")
            .addPathSegment("test")
            .build();

        Request fakeRequest = new Request.Builder()
            .url(url)
            .build();

        // Simulate a server error response
        okhttp3.Response okHttpResponse = mock(okhttp3.Response.class);

        when(mockApiClouderaProperties.getHost()).thenReturn("https://example.com");
        when(mockApiClouderaProperties.getPort()).thenReturn("8080");
        when(mockApiClouderaProperties.getUrl()).thenReturn("/test");
        ComponentProperties.Cpu mockCpu = mock(ComponentProperties.Cpu.class);
        when(mockComponentProperties.getCpu()).thenReturn(mockCpu);
        when(mockCpu.getSelectCpu()).thenReturn("select * from test");
        when(mockCpu.getDesiredRollup()).thenReturn("DAILY");
        when(mockOkHttpClient.newCall(any(Request.class))).thenAnswer(invocation -> {
            Call call = mock(Call.class);
            when(call.execute()).thenReturn(okHttpResponse);
            return call;
        });
        List<Capacity> result = capacityCpuCloudera.findByCapacityCpu(dateFrom, dateTo);
        assertNull(result, "Expected null result on API failure");
    }


}
