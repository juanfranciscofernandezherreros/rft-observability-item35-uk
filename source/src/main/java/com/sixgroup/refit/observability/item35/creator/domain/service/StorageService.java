package com.sixgroup.refit.observability.item35.creator.domain.service;

import com.sixgroup.refit.observability.item35.creator.domain.model.Storage;

import java.util.List;

public interface StorageService {

    List<Storage> getTotalCapacity(String dateFrom, String dateTo);

    List<Storage> getTotalFreeCapacity(String dateFrom, String dateTo);


}
