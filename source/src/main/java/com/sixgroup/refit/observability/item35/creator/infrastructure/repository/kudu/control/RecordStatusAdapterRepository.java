package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.kudu.control;

import com.sixgroup.refit.observability.item35.creator.domain.model.RecordStatus;
import com.sixgroup.refit.observability.item35.creator.domain.repository.control.RecordStatusRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.RecordStatusDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.mappper.RecordStatusMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;


@Repository
@RequiredArgsConstructor
@Slf4j
public class RecordStatusAdapterRepository implements RecordStatusRepository {

    private final RecordStatusKudu recordStatusKudu;

    private final RecordStatusMapper recordStatusMapper;

    @Override
    public List<RecordStatus> findByRecordStatus(final String dateFrom, final String dateTo) {
        log.debug("Find Record status by dateFrom and dateTo");
        final List<RecordStatusDTO> dataFound = recordStatusKudu.findByRecordStatus(dateFrom, dateTo);
        if (null == dataFound || dataFound.isEmpty()) {
            return new ArrayList<>();
        }
        return dataFound.stream().map(recordStatusMapper::entityToDomain).toList();
    }
}
