package com.sixgroup.refit.observability.item.state.infrastructure.repository.sqlserver;

import com.sixgroup.refit.observability.item.state.infrastructure.entity.ItemReportingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SqlServerItemReportingRepository extends JpaRepository<ItemReportingEntity, Integer> {
    ItemReportingEntity findFirstByItemTypeAndFileName(String itemType, String fileName);
}
