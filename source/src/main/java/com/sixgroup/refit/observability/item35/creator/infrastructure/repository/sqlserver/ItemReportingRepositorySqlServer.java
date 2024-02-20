package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.sqlserver;

import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.sqlserver.ItemReportingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemReportingRepositorySqlServer extends JpaRepository<ItemReportingEntity, Long> {

}
