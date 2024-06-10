package com.sixgroup.refit.observability.item35.creator.infrastructure.mappper;

import com.sixgroup.refit.observability.item35.creator.domain.model.ReguIdentityDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.account.ReguIdentityEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReguIdentityMapper {

    ReguIdentityDTO entityToDomain(final ReguIdentityEntity reguIdentityEntity);

    List<ReguIdentityDTO> entitiesToDomains(final List<ReguIdentityEntity> reguIdentityEntities);
}
