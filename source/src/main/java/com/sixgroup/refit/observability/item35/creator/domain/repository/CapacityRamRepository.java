package com.sixgroup.refit.observability.item35.creator.domain.repository;

import com.sixgroup.refit.observability.item35.creator.domain.model.Capacity;

import java.util.List;

public interface CapacityRamRepository {

    List<Capacity> findByCapacityRam(String dateFrom, String dateTo);
}
