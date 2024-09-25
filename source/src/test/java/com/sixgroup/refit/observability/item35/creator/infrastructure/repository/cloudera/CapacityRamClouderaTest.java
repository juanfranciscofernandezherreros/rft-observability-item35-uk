package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.cloudera;

import com.sixgroup.refit.observability.item35.creator.configuration.ApiClouderaProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.ClouderaProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.Capacity;
import okhttp3.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CapacityRamClouderaTest {

    @Mock
    private OkHttpClient mockOkHttpClient;

    @Mock
    private ApiClouderaProperties mockApiClouderaProperties;

    @Mock
    private ClouderaProperties mockClouderaProperties;

    @Mock
    private Call mockCall;

    @InjectMocks
    private CapacityRamCloudera capacityRamCloudera;

    @Test
    void testFindByCapacityRamSuccess() throws IOException {
        String testDateFrom = "2020-01-01";
        String testDateTo = "2020-01-31";
        String testResponseBody = "{\"data\": [{\"value\": 100, \"date\": \"2020-01-01\"}]}";
        Response mockResponse = new Response.Builder()
            .request(new Request.Builder().url("http://test").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(ResponseBody.create(testResponseBody, MediaType.get("application/json")))
            .build();

        when(mockOkHttpClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);
        when(mockApiClouderaProperties.getHost()).thenReturn("http://localhost");
        when(mockApiClouderaProperties.getPort()).thenReturn("8080");
        when(mockApiClouderaProperties.getUrl()).thenReturn("/api/test");
        ClouderaProperties.Ram mockRam = mock(ClouderaProperties.Ram.class);
        when(mockClouderaProperties.getRam()).thenReturn(mockRam);
        when(mockRam.getSelectRam()).thenReturn("select * from test");
        when(mockRam.getDesiredRollup()).thenReturn("DAILY");
        List<Capacity> result = capacityRamCloudera.findByCapacityRam(testDateFrom, testDateTo);
        assertNotNull(result);
        verify(mockOkHttpClient).newCall(any(Request.class));
    }

    @Test
    void testFindByCapacityRamFailure() {
        // Setup
        String dateFrom = "2020-01-01";
        String dateTo = "2020-01-02";

        // Simulate a server error response
        okhttp3.Response okHttpResponse = mock(okhttp3.Response.class);

        when(mockApiClouderaProperties.getHost()).thenReturn("http://example.com");
        when(mockApiClouderaProperties.getPort()).thenReturn("8080");
        when(mockApiClouderaProperties.getUrl()).thenReturn("/test");
        ClouderaProperties.Ram mockRam = mock(ClouderaProperties.Ram.class);
        when(mockClouderaProperties.getRam()).thenReturn(mockRam);
        when(mockRam.getSelectRam()).thenReturn("select * from test");
        when(mockRam.getDesiredRollup()).thenReturn("DAILY");
        when(mockOkHttpClient.newCall(any(Request.class))).thenAnswer(invocation -> {
            Call call = mock(Call.class);
            when(call.execute()).thenReturn(okHttpResponse);
            return call;
        });
        List<Capacity> result = capacityRamCloudera.findByCapacityRam(dateFrom, dateTo);
        assertTrue(result.isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnIllegalArgumentExceptionWhenHostDateFromIsNullOrBlank(String dateFrom) {
        final ClouderaProperties.Ram mockRam = mock(ClouderaProperties.Ram.class);
        when(mockClouderaProperties.getRam()).thenReturn(mockRam);
        assertThrows(IllegalArgumentException.class, () -> capacityRamCloudera.findByCapacityRam(dateFrom, "2020-01-31"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnIllegalArgumentExceptionWhenHostDateToIsNullOrBlank(String dateTo) {
        final ClouderaProperties.Ram mockRam = mock(ClouderaProperties.Ram.class);
        when(mockClouderaProperties.getRam()).thenReturn(mockRam);
        assertThrows(IllegalArgumentException.class, () -> capacityRamCloudera.findByCapacityRam("2020-01-31", dateTo));
    }

    @Test
    void shouldReturnIllegalArgumentExceptionWhenHostPropertiesIsNull() {
        String testDateFrom = "2020-01-01";
        String testDateTo = "2020-01-31";

        final ClouderaProperties.Ram mockRam = mock(ClouderaProperties.Ram.class);
        when(mockClouderaProperties.getRam()).thenReturn(mockRam);
        when(mockApiClouderaProperties.getHost()).thenReturn(null);
        when(mockApiClouderaProperties.getPort()).thenReturn("8080");
        when(mockApiClouderaProperties.getUrl()).thenReturn("/api/test");

        assertThrows(IllegalArgumentException.class, () -> capacityRamCloudera.findByCapacityRam(testDateFrom, testDateTo));
    }

    @Test
    void shouldReturnIllegalArgumentExceptionWhenPortPropertiesIsNull() {
        String testDateFrom = "2020-01-01";
        String testDateTo = "2020-01-31";

        final ClouderaProperties.Ram mockRam = mock(ClouderaProperties.Ram.class);
        when(mockClouderaProperties.getRam()).thenReturn(mockRam);
        when(mockApiClouderaProperties.getHost()).thenReturn("https://example.com");
        when(mockApiClouderaProperties.getPort()).thenReturn(null);
        when(mockApiClouderaProperties.getUrl()).thenReturn("/api/test");

        assertThrows(IllegalArgumentException.class, () -> capacityRamCloudera.findByCapacityRam(testDateFrom, testDateTo));
    }

    @Test
    void shouldReturnIllegalArgumentExceptionWhenUrlPropertiesIsNull() {
        String testDateFrom = "2020-01-01";
        String testDateTo = "2020-01-31";

        final ClouderaProperties.Ram mockRam = mock(ClouderaProperties.Ram.class);
        when(mockClouderaProperties.getRam()).thenReturn(mockRam);
        when(mockApiClouderaProperties.getHost()).thenReturn("https://example.com");
        when(mockApiClouderaProperties.getPort()).thenReturn("8080");
        when(mockApiClouderaProperties.getUrl()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> capacityRamCloudera.findByCapacityRam(testDateFrom, testDateTo));
    }

}
