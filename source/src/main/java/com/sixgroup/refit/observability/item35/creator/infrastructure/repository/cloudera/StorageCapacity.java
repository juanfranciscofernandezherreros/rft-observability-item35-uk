package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.cloudera;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixgroup.refit.observability.item35.creator.configuration.ApiClouderaProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.ClouderaProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.StorageCapacityResponse;
import com.sixgroup.refit.observability.item35.creator.domain.repository.StorageCapacityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;

import java.util.Objects;

import static com.sixgroup.refit.observability.item35.creator.shared.ErrorCatalog.ERROR_CALL_CLOUDERA;
import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.*;

@Repository
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
    value = "component-config.cloudera.storage.enabled",
    havingValue = "true")
public class StorageCapacity implements StorageCapacityRepository {

    private final OkHttpClient okHttpClient;
    private final ApiClouderaProperties apiClouderaProperties;
    private final ClouderaProperties clouderaProperties;

    public StorageCapacityResponse findTotalStorage(String dateFrom, String dateTo) {
        return doClouderaCaller(dateFrom, dateTo, clouderaProperties.getStorage().getSelectTotalApi());
    }

    public StorageCapacityResponse findFreeStorage(String dateFrom, String dateTo) {
        return doClouderaCaller(dateFrom, dateTo, clouderaProperties.getStorage().getSelectFreeApi());
    }

    private StorageCapacityResponse doClouderaCaller(String dateFrom, String dateTo, String query) {
        HttpUrl.Builder urlBuilder
            = HttpUrl.parse(apiClouderaProperties.getHost() + ":" + apiClouderaProperties.getPort() + apiClouderaProperties.getUrl()).newBuilder();
        urlBuilder.addQueryParameter(QUERY, query);
        urlBuilder.addQueryParameter(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        urlBuilder.addQueryParameter(DATE_FROM, dateFrom);
        urlBuilder.addQueryParameter(DATE_TO, dateTo);
        urlBuilder.addQueryParameter(DESIRED_ROLLUP, "DAILY");

        Request request = new Request.Builder()
            .url(urlBuilder.build().toString())
            .method(HttpMethod.GET.name(), null)
            .build();
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        okhttp3.Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                log.error("Error to call Cloudera Storage with message: {}, and code: {}", response.message(), ERROR_CALL_CLOUDERA);
                return null;
            }
            if (Objects.isNull(response.body())) {
                log.error("Error to call Cloudera Storage - Message is null, and code: {}", ERROR_CALL_CLOUDERA);
                return null;
            }
            log.info("StorageCapacity response: {}", response.body());
            return objectMapper.readValue(response.body().string(), StorageCapacityResponse.class);
        } catch (Exception e) {
            final String api = query.split("\\s+")[1];
            log.error("Error to call Cloudera Storage with message: {}, api: {}, code: {}, exception: ",
                e.getMessage(), api, ERROR_CALL_CLOUDERA, e);
            return null;
        } finally {
            if (Objects.nonNull(response)) {
                response.close();
            }
        }
    }

}
