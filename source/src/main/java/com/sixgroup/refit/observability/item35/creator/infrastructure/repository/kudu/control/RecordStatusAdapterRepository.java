package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.kudu.control;

import com.sixgroup.refit.observability.item35.creator.domain.model.RecordStatus;
import com.sixgroup.refit.observability.item35.creator.domain.repository.control.RecordStatusRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.RecordStatusDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.mappper.RecordStatusMapper;
import com.sixgroup.refit.observability.item35.creator.shared.utils.LazyIterators;
import com.sixgroup.refit.observability.item35.creator.shared.utils.PagedIterator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;


@Repository
@RequiredArgsConstructor
@Slf4j
public class RecordStatusAdapterRepository implements RecordStatusRepository {

    private static final int STREAM_PAGE_SIZE = 50_000;

    private final RecordStatusKudu recordStatusKudu;

    private final RecordStatusMapper recordStatusMapper;

    @Override
    public Iterator<RecordStatus> iterateByRecordStatus(final String dateFrom, final String dateTo) {
        log.debug("Find Record status by dateFrom and dateTo");
        Iterator<RecordStatusDTO> records = new PagedIterator<>(
            pageable -> recordStatusKudu.findByRecordStatus(dateFrom, dateTo, pageable), STREAM_PAGE_SIZE);
        return LazyIterators.filterMap(records,
            record -> java.util.Optional.of(recordStatusMapper.entityToDomain(record)));
    }

    @Override
    public List<RecordStatus> findByRecordStatus(final String dateFrom, final String dateTo) {
        List<RecordStatus> results = new ArrayList<>();
        iterateByRecordStatus(dateFrom, dateTo).forEachRemaining(results::add);
        return results;
    }
}
