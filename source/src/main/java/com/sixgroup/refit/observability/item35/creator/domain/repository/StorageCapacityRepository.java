package com.sixgroup.refit.observability.item35.creator.domain.repository;

import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.StorageCapacityResponse;

public interface StorageCapacityRepository {
    StorageCapacityResponse findTotalStorage(String dateFrom, String dateTo);
    StorageCapacityResponse findFreeStorage(String dateFrom, String dateTo);
}
