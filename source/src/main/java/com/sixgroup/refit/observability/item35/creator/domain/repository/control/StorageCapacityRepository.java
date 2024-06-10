package com.sixgroup.refit.observability.item35.creator.domain.repository.control;

import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.StorageCapacityResponse;

import java.util.Optional;

public interface StorageCapacityRepository {
    Optional<StorageCapacityResponse> findTotalStorage(String dateFrom, String dateTo);

    Optional<StorageCapacityResponse> findFreeStorage(String dateFrom, String dateTo);
}
