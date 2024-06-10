package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.kudu.account;

import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.account.ReguIdentityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReguIdentityKudu extends JpaRepository<ReguIdentityEntity, Long> {

    List<ReguIdentityEntity> findByTraceCodeIn(final List<String> traceCode);
}
