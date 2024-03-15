package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.cloudera;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixgroup.refit.observability.item35.creator.configuration.ApiClouderaProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.Response;
import com.sixgroup.refit.observability.item35.creator.domain.repository.StorageCapacityRepository;
import com.sixgroup.refit.observability.modules.log.rft.application.RftLog;
import com.sixgroup.refit.observability.modules.log.rft.domain.logobject.base.NameObject;
import lombok.RequiredArgsConstructor;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;

import static com.sixgroup.refit.observability.item35.creator.shared.ErrorCatalog.ERROR_CALL_CLOUDERA;
import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.*;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(
    value = "component-config.api.cloudera.storage.enabled",
    havingValue = "true")
public class StorageCapacity implements StorageCapacityRepository {

    private final OkHttpClient okHttpClient;

    private final ApiClouderaProperties apiClouderaProperties;

    public Response findTotalStorage(String dateFrom, String dateTo) {
        return doClouderaCaller(dateFrom, dateTo, apiClouderaProperties.getStorage().getSelectTotalApi());
    }

    public Response findFreeStorage(String dateFrom, String dateTo) {
        return doClouderaCaller(dateFrom, dateTo, apiClouderaProperties.getStorage().getSelectFreeApi());
    }

    private Response doClouderaCaller(String dateFrom, String dateTo, String query) {
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
        ResponseBody responseBody = null;
        try {
            responseBody = okHttpClient.newCall(request).execute().body();
            return objectMapper.readValue(responseBody.string(), Response.class);
        } catch (Exception e) {
            String api = query.split("\\s+")[1];
            RftLog.error("Error to call Cloudera Storage",
                List.of(NameObject.builder().name("Error").object(e.getMessage()).build(),
                    NameObject.builder().name("Api").object(api).build()), ERROR_CALL_CLOUDERA);
            throw new RuntimeException("Error to call Cloudera Storage with select " + api, e);
        }
    }

}
