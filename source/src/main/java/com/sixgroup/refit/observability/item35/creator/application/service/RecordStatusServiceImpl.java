package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.domain.model.RecordStatus;
import com.sixgroup.refit.observability.item35.creator.domain.repository.RecordStatusRepository;
import com.sixgroup.refit.observability.item35.creator.domain.service.RecordStatusService;
import com.sixgroup.refit.observability.item35.creator.shared.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class RecordStatusServiceImpl implements RecordStatusService {

    private final RecordStatusRepository recordStatusRepository;
    @Override
    public List<RecordStatus> findRecordStatus() {
        return recordStatusRepository.
                findByRecordStatus(Utils.getFirstDayOfPreviousMonth(LocalDate.now()),Utils.getLastDayOfPreviousMonth(LocalDate.now()));
    }

}
