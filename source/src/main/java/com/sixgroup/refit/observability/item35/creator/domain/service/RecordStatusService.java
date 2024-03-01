package com.sixgroup.refit.observability.item35.creator.domain.service;

import com.sixgroup.refit.observability.item35.creator.domain.model.RecordStatus;

import java.util.List;

public interface RecordStatusService {

     List<RecordStatus> findRecordStatus(String itemDate);
}
