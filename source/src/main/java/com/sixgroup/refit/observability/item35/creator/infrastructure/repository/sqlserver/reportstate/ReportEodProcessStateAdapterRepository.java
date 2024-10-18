package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.sqlserver.reportstate;

import com.sixgroup.refit.observability.item35.creator.domain.repository.reportstate.ReportEodProcessStateRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.sqlserver.ReportEoDDTO;
import com.sixgroup.refit.observability.item35.creator.shared.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ReportEodProcessStateAdapterRepository implements ReportEodProcessStateRepository {
    private final SqlServerReportEodProcessStateRepository sqlServerReportEodProcessStateRepository;

    @Override
    public List<ReportEoDDTO> find(final String initDate, final String endDate) {
        log.debug("Find Regulators by day and fileType");
        final String startDate = DateUtils.localDateToString(LocalDate.parse(initDate));
        final String finalDate = DateUtils.localDateToString(LocalDate.parse(endDate));
        return sqlServerReportEodProcessStateRepository.findParticipantsByDayAccountAndFileType(startDate, finalDate);
    }
}
