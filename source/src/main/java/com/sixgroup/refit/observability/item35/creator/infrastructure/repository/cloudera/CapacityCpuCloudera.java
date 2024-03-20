package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.cloudera;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixgroup.refit.observability.item35.creator.configuration.ApiClouderaProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.ComponentProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.Capacity;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.Response;
import com.sixgroup.refit.observability.item35.creator.domain.repository.CapacityCpuRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.mappper.CapacityMapper;
import com.sixgroup.refit.observability.modules.log.rft.application.RftLog;
import com.sixgroup.refit.observability.modules.log.rft.domain.logobject.base.NameObject;
import lombok.RequiredArgsConstructor;
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
@ConditionalOnProperty(
    value = "component-config.api.cloudera.cpu.enabled",
    havingValue = "true")
public class CapacityCpuCloudera implements CapacityCpuRepository {

    private final OkHttpClient okHttpClient;
    private final ApiClouderaProperties apiClouderaProperties;
    private final ComponentProperties componentProperties;

    @Override
    public List<Capacity> findByCapacityCpu(String dateFrom, String dateTo) {

        RftLog.info("Find Compute capacity Cpu  by dateFrom and dateTo");

        List<Capacity> listCapacityCpu = null;
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(apiClouderaProperties.getHost() + ":" + apiClouderaProperties.getPort() + apiClouderaProperties.getUrl())).newBuilder();
        urlBuilder.addQueryParameter(QUERY, componentProperties.getCpu().getSelectCpu());
        urlBuilder.addQueryParameter(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        urlBuilder.addQueryParameter(DATE_FROM, dateFrom);
        urlBuilder.addQueryParameter(DATE_TO, dateTo);
        urlBuilder.addQueryParameter(DESIRED_ROLLUP, componentProperties.getCpu().getDesiredRollup());
        Request request = new Request.Builder()
            .url(urlBuilder.build().toString())
            .method(HttpMethod.GET.name(), null)
            .build();
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            okhttp3.Response response = okHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                RftLog.error("Error to call Cloudera",
                    List.of(NameObject.builder().name("Error").object(response.message()).build()), ERROR_CALL_CLOUDERA);
                return null;
            }
            if (Objects.nonNull(response.body())) {
                Response responseBody = objectMapper.readValue(response.body().string(), Response.class);
                listCapacityCpu = CapacityMapper.mapperResponseToListCapacity(responseBody);
            }
        } catch (IOException e) {
            RftLog.error("Error to call Cloudera",
                List.of(NameObject.builder().name("Error").object(e.getMessage()).build()), ERROR_CALL_CLOUDERA);
            return null;
        }

        return listCapacityCpu;
    }
}
