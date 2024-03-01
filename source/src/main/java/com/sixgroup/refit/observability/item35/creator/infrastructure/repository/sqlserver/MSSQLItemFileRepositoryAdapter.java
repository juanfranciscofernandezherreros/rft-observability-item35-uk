package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.sqlserver;

import java.util.List;

import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.sqlserver.ItemReportingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MSSQLItemFileRepositoryAdapter extends JpaRepository<ItemReportingEntity, Long> {
    List<ItemReportingEntity> findByItemTypeInAndFileNameIn(List<String> itemType, List<String> fileName);
    ItemReportingEntity findByItemTypeAndFileName(String itemType, String fileName);

}
