package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.domain.model.RecordStatus;
import com.sixgroup.refit.observability.item35.creator.domain.repository.control.RecordStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RecordStatusService {

    private final RecordStatusRepository recordStatusRepository;

    public List<RecordStatus> findRecordStatus(final String dateFrom, final String dateTo) {
        return recordStatusRepository.findByRecordStatus(dateFrom, dateTo);
    }

}
