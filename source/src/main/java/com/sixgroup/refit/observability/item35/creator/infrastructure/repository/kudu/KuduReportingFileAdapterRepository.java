package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.kudu;

import com.sixgroup.refit.observability.item35.creator.configuration.ParticipantFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.RegulatorFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.TrFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.domain.repository.ReportingFileAdapterRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.ParticipantDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.RegulatorDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.TrDTO;
import com.sixgroup.refit.observability.item35.creator.shared.utils.ReportUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
@RequiredArgsConstructor
@Slf4j
public class KuduReportingFileAdapterRepository implements ReportingFileAdapterRepository {

    private final ReportingFileKudu reportingFileKudu;

    private final ParticipantFileTypeProperties participantFileTypeProperties;
    private final RegulatorFileTypeProperties regulatorFileTypeProperties;
    private final TrFileTypeProperties trFileTypeProperties;

    @Override
    public Optional<List<ParticipantDTO>> findParticipantsByDayAccountAndFileType(final String initDate, final String endDate) {
        log.debug("Find Participants by day and fileType");
        final LocalDateTime startDate = LocalDate.parse(initDate).atStartOfDay();
        final LocalDateTime finalDate = LocalDate.parse(endDate).atStartOfDay();
        return Optional.of(reportingFileKudu.
            findParticipantsByDayAccountAndFileType(startDate, finalDate, ReportUtils.getReportsTypeQuery(participantFileTypeProperties.getReports())));
    }

    @Override
    public Optional<List<RegulatorDTO>> findRegulatorByDayAccountAndFileType(final String initDate, final String endDate) {
        log.debug("Find Regulators by day and fileType");
        final LocalDateTime startDate = LocalDate.parse(initDate).atStartOfDay();
        final LocalDateTime finalDate = LocalDate.parse(endDate).atStartOfDay();
        return Optional.of(reportingFileKudu.
            findRegulatorByDayAccountAndFileType(startDate, finalDate, ReportUtils.getReportsTypeQuery(regulatorFileTypeProperties.getReports())));
    }

    @Override
    public Optional<List<TrDTO>> findTrByDayAccountAndFileType(final String initDate, final String endDate) {
        log.debug("Find Regulators by day and fileType");
        final LocalDateTime startDate = LocalDate.parse(initDate).atStartOfDay();
        final LocalDateTime finalDate = LocalDate.parse(endDate).atStartOfDay();
        return Optional.of(reportingFileKudu
            .findTrByDayAccountAndFileType(startDate, finalDate, ReportUtils.getReportsTypeQuery(trFileTypeProperties.getReports())));
    }

}
