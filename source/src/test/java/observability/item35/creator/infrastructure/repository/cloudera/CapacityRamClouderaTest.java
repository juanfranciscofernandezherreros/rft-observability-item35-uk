package observability.item35.creator.infrastructure.repository.cloudera;

import com.sixgroup.refit.observability.item35.creator.configuration.ApiClouderaProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.Capacity;
import com.sixgroup.refit.observability.item35.creator.infrastructure.repository.cloudera.CapacityRamCloudera;
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
public class CapacityRamClouderaTest {

    @Mock
    private OkHttpClient mockOkHttpClient;

    @Mock
    private ApiClouderaProperties mockApiClouderaProperties;

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
            .body(ResponseBody.create(MediaType.get("application/json"), testResponseBody))
            .build();

        when(mockOkHttpClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);
        when(mockApiClouderaProperties.getHost()).thenReturn("http://localhost");
        when(mockApiClouderaProperties.getPort()).thenReturn("8080");
        when(mockApiClouderaProperties.getUrl()).thenReturn("/api/test");
        ApiClouderaProperties.Ram mockRam = mock(ApiClouderaProperties.Ram.class);
        when(mockApiClouderaProperties.getRam()).thenReturn(mockRam);
        when(mockRam.getSelectRam()).thenReturn("select * from test");
        when(mockRam.getDesiredRollup()).thenReturn("DAILY");
        List<Capacity> result = capacityRamCloudera.findByCapacityRam(testDateFrom, testDateTo);
        assertNotNull(result);
        verify(mockOkHttpClient).newCall(any(Request.class));
    }

    @Test
    void testFindByCapacityRamFailure() throws IOException {
        // Setup
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
        okhttp3.Response okHttpResponse = new okhttp3.Response.Builder()
            .request(fakeRequest)
            .protocol(Protocol.HTTP_1_1)
            .code(500)
            .message("Internal Server Error")
            .build();

        when(mockApiClouderaProperties.getHost()).thenReturn("http://example.com");
        when(mockApiClouderaProperties.getPort()).thenReturn("8080");
        when(mockApiClouderaProperties.getUrl()).thenReturn("/test");
        ApiClouderaProperties.Ram mockRam = mock(ApiClouderaProperties.Ram.class);
        when(mockApiClouderaProperties.getRam()).thenReturn(mockRam);
        when(mockRam.getSelectRam()).thenReturn("select * from test");
        when(mockRam.getDesiredRollup()).thenReturn("DAILY");
        when(mockOkHttpClient.newCall(any(Request.class))).thenAnswer(invocation -> {
            Call call = mock(Call.class);
            when(call.execute()).thenReturn(okHttpResponse);
            return call;
        });
        List<Capacity> result = capacityRamCloudera.findByCapacityRam(dateFrom, dateTo);
        assertNull(result, "Expected null result on API failure");
    }

}
