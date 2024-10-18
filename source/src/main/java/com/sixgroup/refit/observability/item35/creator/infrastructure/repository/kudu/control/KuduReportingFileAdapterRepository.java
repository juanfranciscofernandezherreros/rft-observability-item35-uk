package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.kudu.control;

import com.sixgroup.refit.observability.item35.creator.configuration.ParticipantFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.RegulatorFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.ReportProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.TrFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.domain.config.ReportConfig;
import com.sixgroup.refit.observability.item35.creator.domain.config.TranslationData;
import com.sixgroup.refit.observability.item35.creator.domain.repository.control.ReportingFileAdapterRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.ParticipantDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.RegulatorDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.TrDTO;
import com.sixgroup.refit.observability.item35.creator.shared.utils.ReportUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final ReportProperties reportProperties;

    public List<ParticipantDTO> findParticipantsByDayAccountAndFileType(final String initDate, final String endDate) {
        log.debug("Find Participants by day and fileType");
        final LocalDateTime startDate = LocalDate.parse(initDate).atStartOfDay();
        final LocalDateTime finalDate = LocalDate.parse(endDate).atStartOfDay();
        final List<ReportConfig> reports = participantFileTypeProperties.getReports();
        final List<String> reportsCustom = participantFileTypeProperties.getReportsCustom().stream().map(ReportConfig::getName).toList();
        final List<ReportConfig> reportsQuery = reports.stream().filter(report -> !reportsCustom.contains(report.getName())).toList();
        if (reportsQuery.isEmpty()) {
            return new ArrayList<>();
        }
        return reportingFileKudu.
            findParticipantsByDayAccountAndFileType(startDate, finalDate, ReportUtils.getReportsTypeQuery(reportsQuery));
    }

    public List<ParticipantDTO> findParticipantsRecoFileType(final String initDate, final String endDate) {
        log.debug("Find Participants reco by day and fileType");
        final LocalDateTime startDate = LocalDate.parse(initDate).atStartOfDay();
        final LocalDateTime finalDate = LocalDate.parse(endDate).atStartOfDay();
        final List<ReportConfig> reports = participantFileTypeProperties.getReports();
        final List<String> reportsCustom = participantFileTypeProperties.getReportsCustom().stream().map(ReportConfig::getName).toList();
        final List<ReportConfig> reportsQuery = reports.stream().filter(report -> reportsCustom.contains(report.getName())).toList();
        if (reportsQuery.isEmpty()) {
            return new ArrayList<>();
        }
        final List<ParticipantDTO> participantsFound = reportingFileKudu.
            findParticipantsRecoFileType(startDate, finalDate, ReportUtils.getReportsTypeQuery(reportsQuery));
        if (participantsFound.isEmpty()) {
            return new ArrayList<>();
        }
        for (ParticipantDTO participant : participantsFound) {
            final Optional<TranslationData> translationFound = reportProperties.getTranslation().getReports().stream().filter(report -> report.getName().equals(participant.getFileType())).findFirst();
            translationFound.ifPresent(translationData -> participant.setFileType(translationData.getValue()));
        }
        return participantsFound;
    }

    public List<RegulatorDTO> findRegulatorByDayAccountAndFileType(final String initDate, final String endDate) {
        log.debug("Find Regulators by day and fileType");
        final LocalDateTime startDate = LocalDate.parse(initDate).atStartOfDay();
        final LocalDateTime finalDate = LocalDate.parse(endDate).atStartOfDay();
        return reportingFileKudu.
            findRegulatorByDayAccountAndFileType(startDate, finalDate, ReportUtils.getReportsTypeQuery(regulatorFileTypeProperties.getReports()));
    }

    public List<TrDTO> findTrByDayAccountAndFileType(final String initDate, final String endDate) {
        log.debug("Find Regulators by day and fileType");
        final LocalDateTime startDate = LocalDate.parse(initDate).atStartOfDay();
        final LocalDateTime finalDate = LocalDate.parse(endDate).atStartOfDay();
        return reportingFileKudu
            .findTrByDayAccountAndFileType(startDate, finalDate, ReportUtils.getReportsTypeQuery(trFileTypeProperties.getReports()));
    }

}
