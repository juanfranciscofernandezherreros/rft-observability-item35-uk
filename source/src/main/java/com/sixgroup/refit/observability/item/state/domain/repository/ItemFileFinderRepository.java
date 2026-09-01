package com.sixgroup.refit.observability.item.state.domain.repository;

import com.sixgroup.refit.observability.item.state.domain.model.ItemFileFinderRequest;
import com.sixgroup.refit.observability.item.state.domain.model.ItemReportingDto;

import java.util.List;

public interface ItemFileFinderRepository {
    List<ItemReportingDto> findAllByItemTypeAndFileName(List<ItemFileFinderRequest> requests);
    ItemReportingDto findByItemTypeAndFileName(String itemType, String fileName);
    ItemReportingDto save(ItemReportingDto itemReporting);
}
