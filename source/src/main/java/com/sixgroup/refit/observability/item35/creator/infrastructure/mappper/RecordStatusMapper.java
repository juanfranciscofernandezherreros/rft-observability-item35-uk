package com.sixgroup.refit.observability.item35.creator.infrastructure.mappper;

import com.sixgroup.refit.observability.item35.creator.domain.model.RecordStatus;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.RecordStatusDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecordStatusMapper {

    @Mapping(target = "noMessagesOnGiveDate", source = "count")
    @Mapping(target = "reportingDate", source = "reportingDate", dateFormat = "yyyy-MM-dd")
    RecordStatus entityToDomain(RecordStatusDTO recordStatusEntity);

}
