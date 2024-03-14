package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.cloudera;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixgroup.refit.observability.item35.creator.domain.model.Capacity;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.Response;
import com.sixgroup.refit.observability.item35.creator.domain.repository.CapacityCpuRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.mappper.CapacityMapper;
import com.sixgroup.refit.observability.modules.log.rft.application.RftLog;
import com.sixgroup.refit.observability.modules.log.rft.domain.logobject.base.NameObject;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;

import static com.sixgroup.refit.observability.item35.creator.shared.ErrorCatalog.ERROR_CALL_CLOUDERA;


@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(
    value = "component-config.api.cloudera.cpu.enabled",
    havingValue = "false")
public class CapacityCpuClouderaMock implements CapacityCpuRepository {

    @Override
    public List<Capacity> findByCapacityCpu(String dateFrom, String dateTo) {
        List<Capacity> listCapacityCpu = null;
        try {
            Response response = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(getClass().getClassLoader().getResourceAsStream("json/capacity-cpu.json"), Response.class);
            listCapacityCpu = CapacityMapper.mapperResponseToListCapacity(response);
        } catch (IOException e) {
            RftLog.error("Error to call Cloudera CPU Mock",
                List.of(NameObject.builder().name("Error").object(e.getMessage()).build()), ERROR_CALL_CLOUDERA);
        }
        return listCapacityCpu;
    }
}
