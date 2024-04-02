package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.cloudera;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixgroup.refit.observability.item35.creator.domain.model.Capacity;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.Response;
import com.sixgroup.refit.observability.item35.creator.domain.repository.CapacityRamRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.mappper.CapacityMapper;
import com.sixgroup.refit.observability.modules.log.rft.application.RftLog;
import com.sixgroup.refit.observability.modules.log.rft.domain.logobject.base.NameObject;
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
    value = "component-config.api.cloudera.ram.enabled",
    havingValue = "false")
public class CapacityRamClouderaMock implements CapacityRamRepository {
    @Override
    public List<Capacity> findByCapacityRam(String dateFrom, String dateTo) {
        List<Capacity> listCapacityRam = null;
        try {
            Response response = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(getClass().getClassLoader().getResourceAsStream("json/capacity-ram.json"), Response.class);
            listCapacityRam = CapacityMapper.mapperResponseToListCapacity(response);
        } catch (IOException e) {
            log.error("Error to call Cloudera Ram Mock with message: {}, and code: {}",
                e.getMessage(), ERROR_CALL_CLOUDERA);
        }
        return listCapacityRam;
    }
}
