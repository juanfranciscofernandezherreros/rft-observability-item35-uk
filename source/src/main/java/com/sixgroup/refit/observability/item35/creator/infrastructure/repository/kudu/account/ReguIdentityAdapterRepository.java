package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.kudu.account;

import com.sixgroup.refit.observability.item35.creator.domain.model.ReguIdentityDTO;
import com.sixgroup.refit.observability.item35.creator.domain.repository.account.ReguIdentityRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.account.ReguIdentityEntity;
import com.sixgroup.refit.observability.item35.creator.infrastructure.mappper.ReguIdentityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ReguIdentityAdapterRepository implements ReguIdentityRepository {

    private final ReguIdentityKudu reguIdentityKudu;

    private final ReguIdentityMapper reguIdentityMapper;

    @Override
    public List<ReguIdentityDTO> findByTraceCode(final List<String> traceCodes) {

        List<ReguIdentityEntity> reguIdentityEntities = reguIdentityKudu.findByTraceCodeIn(traceCodes);

        return reguIdentityMapper.entitiesToDomains(reguIdentityEntities);
    }
}
