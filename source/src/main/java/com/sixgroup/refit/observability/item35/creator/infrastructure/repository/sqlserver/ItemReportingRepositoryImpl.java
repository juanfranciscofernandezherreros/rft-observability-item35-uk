package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.sqlserver;

import com.sixgroup.refit.observability.item35.creator.domain.model.ItemReporting;
import com.sixgroup.refit.observability.item35.creator.domain.repository.ItemReportingRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.sqlserver.ItemReportingEntity;
import com.sixgroup.refit.observability.item35.creator.infrastructure.mappper.ItemReportingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ItemReportingRepositoryImpl implements ItemReportingRepository {

    private final ItemReportingRepositorySqlServer itemReportingRepositorySqlServer;

    private final ItemReportingMapper itemReportingMapper;

    @Override
    public void insertItemReporting(ItemReporting itemReporting) {
        itemReportingRepositorySqlServer.save(itemReportingMapper.domainToEntity(itemReporting));
    }


}
