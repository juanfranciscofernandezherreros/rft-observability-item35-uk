package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.kudu;

import com.sixgroup.refit.observability.item35.creator.domain.model.RecordStatus;
import com.sixgroup.refit.observability.item35.creator.infrastructure.mappper.RecordStatusMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@RequiredArgsConstructor
@Slf4j
public class RecordStatusRepository implements com.sixgroup.refit.observability.item35.creator.domain.repository.RecordStatusRepository {

    private final RecordStatusKudu recordStatusKudu;

    private final RecordStatusMapper recordStatusMapper;

    @Override
    public List<RecordStatus> findByRecordStatus(String dateFrom, String dateTo) {
        log.debug("Find Record status by dateFrom and dateTo");
        return recordStatusKudu.findByRecordStatus(dateFrom, dateTo).stream().map(recordStatusMapper::entityToDomain).toList();
    }
}
