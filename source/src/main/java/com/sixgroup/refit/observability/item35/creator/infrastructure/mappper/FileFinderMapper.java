package com.sixgroup.refit.observability.item35.creator.infrastructure.mappper;

import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCreateFileRequest;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemReportingDto;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.sqlserver.ItemReportingEntity;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED)
public interface FileFinderMapper {
    ItemReportingDto entityMssqlToDomain(ItemReportingEntity itemReportingEntity);
    ItemReportingEntity requestToEntity (ItemCreateFileRequest request);
    ItemReportingEntity itemReportingDtoToEntity (ItemReportingDto request);

}
