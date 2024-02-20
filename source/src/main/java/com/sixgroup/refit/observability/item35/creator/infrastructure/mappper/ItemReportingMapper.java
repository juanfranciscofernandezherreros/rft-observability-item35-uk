package com.sixgroup.refit.observability.item35.creator.infrastructure.mappper;

import com.sixgroup.refit.observability.item35.creator.domain.model.ItemReporting;
import com.sixgroup.refit.observability.item35.creator.domain.model.RecordStatus;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.RecordStatusDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.sqlserver.ItemReportingEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ItemReportingMapper {
    ItemReportingEntity domainToEntity(ItemReporting itemReporting);

}
