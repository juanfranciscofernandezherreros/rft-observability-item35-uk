package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.cloudera;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixgroup.refit.observability.item35.creator.configuration.ApiClouderaProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.ComponentProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.Capacity;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.StorageCapacityResponse;
import com.sixgroup.refit.observability.item35.creator.domain.repository.CapacityRamRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.mappper.CapacityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.sixgroup.refit.observability.item35.creator.shared.ErrorCatalog.ERROR_CALL_CLOUDERA;
import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.*;

@Repository
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
    value = "component-config.api.cloudera.ram.enabled",
    havingValue = "true")
public class CapacityRamCloudera implements CapacityRamRepository {

    private final OkHttpClient okHttpClient;
    private final ApiClouderaProperties apiClouderaProperties;
    private final ComponentProperties componentProperties;

    @Override
    public List<Capacity> findByCapacityRam(String dateFrom, String dateTo) {

        log.debug("Find Compute capacity Ram by dateFrom and dateTo");

        List<Capacity> listCapacityRam = null;
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(apiClouderaProperties.getHost() + ":" + apiClouderaProperties.getPort() + apiClouderaProperties.getUrl())).newBuilder();
        urlBuilder.addQueryParameter(QUERY, componentProperties.getRam().getSelectRam());
        urlBuilder.addQueryParameter(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        urlBuilder.addQueryParameter(DATE_FROM, dateFrom);
        urlBuilder.addQueryParameter(DATE_TO, dateTo);
        urlBuilder.addQueryParameter(DESIRED_ROLLUP, componentProperties.getRam().getDesiredRollup());

        Request request = new Request.Builder()
            .url(urlBuilder.build().toString())
            .method(HttpMethod.GET.name(), null)
            .build();
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        okhttp3.Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                log.error("Error to call Cloudera with message: {}, and code: {}", response.message(), ERROR_CALL_CLOUDERA);
                return null;
            }
            if (Objects.nonNull(response.body())) {
                StorageCapacityResponse storageCapacityResponseBody = objectMapper.readValue(response.body().string(), StorageCapacityResponse.class);
                listCapacityRam = CapacityMapper.mapperResponseToListCapacity(storageCapacityResponseBody);
            }
        } catch (IOException e) {
            log.error("Error to call Cloudera Ram with message: {}, code: {}, exception: ",
                e.getMessage(), ERROR_CALL_CLOUDERA, e);
            return null;
        } finally {
            if (Objects.nonNull(response)) {
                response.close();
            }
        }
        return listCapacityRam;
    }
}
