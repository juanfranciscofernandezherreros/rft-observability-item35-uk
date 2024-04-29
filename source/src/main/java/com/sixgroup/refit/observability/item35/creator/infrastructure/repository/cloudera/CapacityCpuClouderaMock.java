package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.cloudera;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixgroup.refit.observability.item35.creator.domain.model.Capacity;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.StorageCapacityResponse;
import com.sixgroup.refit.observability.item35.creator.domain.repository.CapacityCpuRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.mappper.CapacityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;

import static com.sixgroup.refit.observability.item35.creator.shared.ErrorCatalog.ERROR_CALL_CLOUDERA;


@Repository
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
    value = "component-config.api.cloudera.cpu.enabled",
    havingValue = "false")
public class CapacityCpuClouderaMock implements CapacityCpuRepository {

    @Override
    public List<Capacity> findByCapacityCpu(String dateFrom, String dateTo) {
        List<Capacity> listCapacityCpu = null;
        try {
            StorageCapacityResponse storageCapacityResponse = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(getClass().getClassLoader().getResourceAsStream("json/capacity-cpu.json"), StorageCapacityResponse.class);
            listCapacityCpu = CapacityMapper.mapperResponseToListCapacity(storageCapacityResponse);
        } catch (IOException e) {
            log.error("Error to call Cloudera CPU Mock with message: {}, and code: {}",
                e.getMessage(), ERROR_CALL_CLOUDERA);
        }
        return listCapacityCpu;
    }
}
