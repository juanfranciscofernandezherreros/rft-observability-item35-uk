package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.kudu;

import com.sixgroup.refit.observability.item35.creator.domain.model.RecordStatus;
import com.sixgroup.refit.observability.item35.creator.infrastructure.mappper.RecordStatusMapper;
import com.sixgroup.refit.observability.modules.log.rft.application.RftLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@RequiredArgsConstructor
public class RecordStatusRepository implements com.sixgroup.refit.observability.item35.creator.domain.repository.RecordStatusRepository {

    private final RecordStatusKudu recordStatusKudu;

    private final RecordStatusMapper recordStatusMapper;

    @Override
    public List<RecordStatus> findByRecordStatus(String dateFrom, String dateTo) {
        RftLog.info("Find Record status by dateFrom and dateTo");
        return recordStatusKudu.
            findByRecordStatus(dateFrom, dateTo)
            .stream().map(recordStatusMapper::entityToDomain).toList();
    }
}
