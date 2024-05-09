package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.cloudera;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.StorageCapacityResponse;
import com.sixgroup.refit.observability.item35.creator.domain.repository.StorageCapacityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.IOException;

import static com.sixgroup.refit.observability.item35.creator.shared.ErrorCatalog.ERROR_CALL_CLOUDERA;


@Repository
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
    value = "component-config.cloudera.storage.enabled",
    havingValue = "false")
public class StorageCapacityMock implements StorageCapacityRepository {

    @Override
    public StorageCapacityResponse findTotalStorage(String dateFrom, String dateTo) {
        try {
            return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(getClass().getClassLoader().getResourceAsStream("json/total_all_MBT.json"),
                    StorageCapacityResponse.class);
        } catch (IOException e) {
            log.error("Error to call 'findTotalStorage()' Cloudera Storage Mock, with message: {}, and code: {}",
                e.getMessage(), ERROR_CALL_CLOUDERA);
            throw new RuntimeException(e);
        }
    }

    @Override
    public StorageCapacityResponse findFreeStorage(String dateFrom, String dateTo) {
        try {
            return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(getClass().getClassLoader().getResourceAsStream("json/free_all_MBT.json"),
                    StorageCapacityResponse.class);
        } catch (IOException e) {
            log.error("Error to call 'findFreeStorage()' Cloudera Storage Mock, with message: {}, and code: {}",
                e.getMessage(), ERROR_CALL_CLOUDERA);
            throw new RuntimeException(e);
        }
    }
}
