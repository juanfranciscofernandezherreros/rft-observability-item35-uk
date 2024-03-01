package com.sixgroup.refit.observability.item35.creator.domain.repository;

import com.sixgroup.refit.observability.item35.creator.domain.model.ItemFileFinderRequest;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemReportingDto;

import java.util.List;

public interface ItemFileFinderRepository {
    List<ItemReportingDto> findAllByItemTypeAndFileName(final List<ItemFileFinderRequest> request);

    ItemReportingDto findByItemTypeAndFileName(final String itemType, final String fileName);

    ItemReportingDto save(final ItemReportingDto request);

}
