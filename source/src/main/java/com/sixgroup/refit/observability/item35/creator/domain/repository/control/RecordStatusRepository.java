package com.sixgroup.refit.observability.item35.creator.domain.repository.control;

import com.sixgroup.refit.observability.item35.creator.domain.model.RecordStatus;

import java.util.Iterator;
import java.util.List;

public interface RecordStatusRepository {

    List<RecordStatus> findByRecordStatus(String dateFrom, String dateTo);

    default Iterator<RecordStatus> iterateByRecordStatus(String dateFrom, String dateTo) {
        return findByRecordStatus(dateFrom, dateTo).iterator();
    }
}
