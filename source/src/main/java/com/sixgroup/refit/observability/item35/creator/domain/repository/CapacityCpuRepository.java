package com.sixgroup.refit.observability.item35.creator.domain.repository;

import com.sixgroup.refit.observability.item35.creator.domain.model.Capacity;

import java.util.List;

public interface CapacityCpuRepository {

    List<Capacity> findByCapacityCpu(String dateFrom, String dateTo);
}
