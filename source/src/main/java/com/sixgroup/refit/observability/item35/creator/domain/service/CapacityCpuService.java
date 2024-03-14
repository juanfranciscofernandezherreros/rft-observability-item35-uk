package com.sixgroup.refit.observability.item35.creator.domain.service;

import com.sixgroup.refit.observability.item35.creator.domain.model.Capacity;

import java.util.List;

public interface CapacityCpuService {

    List<Capacity> findByCapacityCpu(String itemDate);
}
