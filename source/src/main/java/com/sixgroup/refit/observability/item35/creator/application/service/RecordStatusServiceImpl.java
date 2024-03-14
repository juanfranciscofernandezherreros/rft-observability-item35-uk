package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.domain.model.RecordStatus;
import com.sixgroup.refit.observability.item35.creator.domain.repository.RecordStatusRepository;
import com.sixgroup.refit.observability.item35.creator.domain.service.RecordStatusService;
import com.sixgroup.refit.observability.item35.creator.shared.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RecordStatusServiceImpl implements RecordStatusService {

    private final RecordStatusRepository recordStatusRepository;

    @Override
    public List<RecordStatus> findRecordStatus(String itemDate) {
        return recordStatusRepository.
            findByRecordStatus(Utils.getFirstDayOfMonthAndYear(itemDate), Utils.getLastDayOfMonthAndYear(itemDate));
    }

}
