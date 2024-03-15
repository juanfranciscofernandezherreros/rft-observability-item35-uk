package com.sixgroup.refit.observability.item35.creator.domain.repository;

import com.sixgroup.refit.observability.item35.creator.domain.model.Storage;
import com.sixgroup.refit.observability.item35.creator.domain.model.storage.response.Response;

import java.util.List;

public interface StorageCapacityRepository {
    Response findTotalStorage(String dateFrom, String dateTo);
    Response findFreeStorage(String dateFrom, String dateTo);
}
