package com.sixgroup.refit.observability.item35.creator.domain.repository.account;


import com.sixgroup.refit.observability.item35.creator.domain.model.ReguIdentityDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.account.ReguIdentityEntity;

import java.util.List;

public interface ReguIdentityRepository {

    List<ReguIdentityDTO> findByTraceCode(final List<String> traceCode);
}
