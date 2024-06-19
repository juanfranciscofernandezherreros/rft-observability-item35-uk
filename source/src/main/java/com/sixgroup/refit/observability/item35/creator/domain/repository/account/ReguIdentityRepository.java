package com.sixgroup.refit.observability.item35.creator.domain.repository.account;


import com.sixgroup.refit.observability.item35.creator.domain.model.ReguIdentityDTO;

import java.util.List;

public interface ReguIdentityRepository {

    List<ReguIdentityDTO> findByTraceCode(final List<String> traceCode);
}
