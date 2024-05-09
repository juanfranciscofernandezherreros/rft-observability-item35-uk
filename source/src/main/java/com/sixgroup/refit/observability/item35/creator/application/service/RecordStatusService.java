package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.domain.model.RecordStatus;
import com.sixgroup.refit.observability.item35.creator.domain.repository.RecordStatusRepository;
import com.sixgroup.refit.observability.item35.creator.shared.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RecordStatusService {

    private final RecordStatusRepository recordStatusRepository;

    public List<RecordStatus> findRecordStatus(String itemDate) {
        return recordStatusRepository.
            findByRecordStatus(DateUtils.firstDayOfMonth(itemDate), DateUtils.lastDayOfMonth(itemDate));
    }

}
