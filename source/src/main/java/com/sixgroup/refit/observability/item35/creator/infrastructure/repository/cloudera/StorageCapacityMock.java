package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.cloudera;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.StorageCapacityResponse;
import com.sixgroup.refit.observability.item35.creator.domain.repository.StorageCapacityRepository;
import com.sixgroup.refit.observability.item35.creator.shared.exception.InternalErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.Optional;

import static com.sixgroup.refit.observability.item35.creator.shared.ErrorCatalog.ERROR_CALL_CLOUDERA;


@Repository
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
    value = "component-config.cloudera.storage.enabled",
    havingValue = "false")
public class StorageCapacityMock implements StorageCapacityRepository {

    @Override
    public Optional<StorageCapacityResponse> findTotalStorage(String dateFrom, String dateTo) {
        try {
            return Optional.of(new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(getClass().getClassLoader().getResourceAsStream("json/total_all_MBT.json"),
                    StorageCapacityResponse.class));
        } catch (IOException e) {
            log.error("Error to call 'findTotalStorage()' Cloudera Storage Mock, with message: {}, and code: {}",
                e.getMessage(), ERROR_CALL_CLOUDERA);
            throw new InternalErrorException("Error to call findTotalStorage", e);
        }
    }

    @Override
    public Optional<StorageCapacityResponse> findFreeStorage(String dateFrom, String dateTo) {
        try {
            return Optional.of(new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(getClass().getClassLoader().getResourceAsStream("json/free_all_MBT.json"),
                    StorageCapacityResponse.class));
        } catch (IOException e) {
            log.error("Error to call 'findFreeStorage()' Cloudera Storage Mock, with message: {}, and code: {}",
                e.getMessage(), ERROR_CALL_CLOUDERA);
            throw new InternalErrorException("Error to call findFreeStorage", e);
        }
    }
}
