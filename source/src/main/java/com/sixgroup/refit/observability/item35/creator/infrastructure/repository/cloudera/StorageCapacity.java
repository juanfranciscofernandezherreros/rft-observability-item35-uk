package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.cloudera;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixgroup.refit.observability.item35.creator.configuration.ApiClouderaProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.ClouderaProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.StorageCapacityResponse;
import com.sixgroup.refit.observability.item35.creator.domain.repository.control.StorageCapacityRepository;
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
import java.util.Optional;
import java.util.function.Predicate;

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

    public Optional<StorageCapacityResponse> findTotalStorage(String dateFrom, String dateTo) {

        ClouderaProperties clouderaPropertiesAux = Optional.ofNullable(clouderaProperties)
            .orElseThrow(() -> new IllegalArgumentException("'clouderaProperties' cannot be null"));

        ClouderaProperties.Storage storage = Optional.ofNullable(clouderaPropertiesAux.getStorage())
            .orElseThrow(() -> new IllegalArgumentException("'storage' cannot be null"));

        String selectTotalApi = Optional.ofNullable(storage.getSelectTotalApi())
            .filter(Predicate.not(String::isBlank))
            .orElseThrow(() -> new IllegalArgumentException("'selectTotalApi' cannot be null or blank"));

        return doClouderaCaller(dateFrom, dateTo, selectTotalApi);
    }

    public Optional<StorageCapacityResponse> findFreeStorage(String dateFrom, String dateTo) {

        ClouderaProperties clouderaPropertiesAux = Optional.ofNullable(clouderaProperties)
            .orElseThrow(() -> new IllegalArgumentException("'clouderaProperties' cannot be null"));

        ClouderaProperties.Storage storage = Optional.ofNullable(clouderaPropertiesAux.getStorage())
            .orElseThrow(() -> new IllegalArgumentException("'storage' cannot be null"));

        String selectFreeApi = Optional.ofNullable(storage.getSelectFreeApi())
            .filter(Predicate.not(String::isBlank))
            .orElseThrow(() -> new IllegalArgumentException("'selectFreeApi' cannot be null or blank"));

        return doClouderaCaller(dateFrom, dateTo, selectFreeApi);
    }

    private Optional<StorageCapacityResponse> doClouderaCaller(String dateFrom, String dateTo, String query) {

        log.debug("Find Storage capacity dateFrom {}, dateTo {} and query {}", dateFrom, dateTo, query);

        String dateFromAux = Optional.ofNullable(dateFrom)
            .filter(Predicate.not(String::isBlank))
            .orElseThrow(() -> new IllegalArgumentException("'dateFrom' cannot be null or blank"));

        String dateToAux = Optional.ofNullable(dateTo)
            .filter(Predicate.not(String::isBlank))
            .orElseThrow(() -> new IllegalArgumentException("'dateTo' cannot be null or blank"));

        String queryAux = Optional.ofNullable(query)
            .filter(Predicate.not(String::isBlank))
            .orElseThrow(() -> new IllegalArgumentException("'query' cannot be null or blank"));

        ApiClouderaProperties apiClouderaPropertiesAux = Optional.ofNullable(apiClouderaProperties)
            .orElseThrow(() -> new IllegalArgumentException("'apiClouderaProperties' cannot be null"));

        String host = Optional.ofNullable(apiClouderaPropertiesAux.getHost())
            .filter(Predicate.not(String::isBlank))
            .orElseThrow(() -> new IllegalArgumentException("'host' cannot be null or blank"));

        String port = Optional.ofNullable(apiClouderaPropertiesAux.getPort())
            .filter(Predicate.not(String::isBlank))
            .orElseThrow(() -> new IllegalArgumentException("'port' cannot be null or blank"));

        String url = Optional.ofNullable(apiClouderaPropertiesAux.getUrl())
            .filter(Predicate.not(String::isBlank))
            .orElseThrow(() -> new IllegalArgumentException("'url' cannot be null or blank"));

        final HttpUrl.Builder urlBuilder = HttpUrl.parse(host + port + url).newBuilder();

        urlBuilder.addQueryParameter(QUERY, queryAux);
        urlBuilder.addQueryParameter(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        urlBuilder.addQueryParameter(DATE_FROM, dateFromAux);
        urlBuilder.addQueryParameter(DATE_TO, dateToAux);
        urlBuilder.addQueryParameter(DESIRED_ROLLUP, "DAILY");

        final Request request = new Request.Builder()
            .url(urlBuilder.build().toString())
            .method(HttpMethod.GET.name(), null)
            .build();
        final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false);
        okhttp3.Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            if (null == response || Objects.isNull(response.body())) {
                log.error("Error to call Cloudera Storage - Message is null, and code: {}", ERROR_CALL_CLOUDERA);
                return Optional.empty();
            }
            if (!response.isSuccessful()) {
                log.error("Error to call Cloudera Storage with message: {}, and code: {}", response.message(), ERROR_CALL_CLOUDERA);
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(response.body().string(), StorageCapacityResponse.class));
        } catch (Exception e) {
            final String api = query.split("\\s+")[1];
            log.error("Error to call Cloudera Storage with message: {}, api: {}, code: {}, exception: ",
                e.getMessage(), api, ERROR_CALL_CLOUDERA, e);
            return Optional.empty();
        } finally {
            if (Objects.nonNull(response)) {
                response.close();
            }
        }
    }

}
