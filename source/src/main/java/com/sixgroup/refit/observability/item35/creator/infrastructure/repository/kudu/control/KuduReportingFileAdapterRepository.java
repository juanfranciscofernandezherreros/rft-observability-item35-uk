package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.kudu.control;

import com.sixgroup.refit.observability.item35.creator.configuration.*;
import com.sixgroup.refit.observability.item35.creator.domain.config.ReportConfig;
import com.sixgroup.refit.observability.item35.creator.domain.config.TranslationData;
import com.sixgroup.refit.observability.item35.creator.domain.repository.control.ReportingFileAdapterRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.ParticipantDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.RegulatorDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.TrDTO;
import com.sixgroup.refit.observability.item35.creator.shared.utils.ReportUtils;
import com.sixgroup.refit.observability.item35.creator.shared.utils.LazyIterators;
import com.sixgroup.refit.observability.item35.creator.shared.utils.PagedIterator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class KuduReportingFileAdapterRepository implements ReportingFileAdapterRepository {

    private static final int STREAM_PAGE_SIZE = 50_000;

    private final ReportingFileKudu reportingFileKudu;
    private final ParticipantProperties participantProperties;
    private final RegulatorProperties regulatorProperties;
    private final TrProperties trProperties;
    private final ReportItemProperties reportProperties;

    public Iterator<ParticipantDTO> iterateParticipantsByDayAccountAndFileType(final String initDate, final String endDate) {
        log.debug("[START] findParticipantsByDayAccountAndFileType | Range: [{} - {}]", initDate, endDate);
        final LocalDateTime startDate = LocalDate.parse(initDate).atStartOfDay();
        final LocalDateTime finalDate = LocalDate.parse(endDate).atStartOfDay();
        log.debug("[PROCESS] | Range: [{} - {}]", startDate, finalDate);
        final List<ReportConfig> reports = participantProperties.getReports();
        log.debug("[REPORTS] | reports: [{}]", reports);
        final List<String> reportsCustom = participantProperties.getReportsCustom().stream().map(ReportConfig::getName).toList();
        log.debug("[REPORT_CUSTOM] | reportsCustom: [{}]", reportsCustom);
        log.debug("[CONFIG] Total reports in properties: {}. Custom reports to exclude: {}", reports.size(), reportsCustom.size());
        final List<ReportConfig> reportsQuery = reports.stream()
            .filter(report -> {
                boolean isNotCustom = !reportsCustom.contains(report.getName());
                if (!isNotCustom) {
                    log.trace("[FILTER] Excluding custom report: {}", report.getName());
                }
                return isNotCustom;
            })
            .toList();
        log.debug("[REPORTSQUERY] | reportsQuery: [{}]", reportsQuery);
        if (reportsQuery.isEmpty()) {
            log.error("[SKIP] No standard reports found after filtering. Returning empty list.");
            return Collections.emptyIterator();
        }
        List<String> reportTypes = ReportUtils.getReportsTypeQuery(reportsQuery);
        log.debug("[REPORT_TYPES] | reportTypes: [{}]", reportTypes);
        long startTime = System.currentTimeMillis();
        log.debug("[START_TIME] | start_time: [{}]", startTime);
        log.debug("[ACCOUNT_ID] | participantProperties.getAccountId(): [{}]", participantProperties.getAccountId());
        return new PagedIterator<>(pageable -> reportingFileKudu.findParticipantsByDayAccountAndFileType(
            startDate, finalDate, reportTypes, participantProperties.getAccountId(), pageable), STREAM_PAGE_SIZE);
    }

    public Iterator<ParticipantDTO> iterateParticipantsRecoFileType(final String initDate, final String endDate) {
        log.debug("[START] findParticipantsRecoFileType | Range: [{} - {}]", initDate, endDate);
        final LocalDateTime startDate = LocalDate.parse(initDate).atStartOfDay();
        final LocalDateTime finalDate = LocalDate.parse(endDate).atStartOfDay();
        log.debug("[PROCESS] | Range: [{} - {}]", startDate, finalDate);
        final List<ReportConfig> reports = participantProperties.getReports();
        log.debug("[REPORTS] | reports: [{}]", reports);
        final List<String> reportsCustomNames = participantProperties.getReportsCustom().stream().map(ReportConfig::getName).toList();
        log.debug("[REPORT_CUSTOM_NAMES] | reportsCustomNames: [{}]", reportsCustomNames);
        final List<ReportConfig> reportsQuery = reports.stream().filter(report -> reportsCustomNames.contains(report.getName())).toList();
        log.debug("[REPORTSQUERY] | reportsQuery: [{}]", reportsQuery);
        if (reportsQuery.isEmpty()) {
            log.error("[SKIP] No custom/RECO reports found for query. Check configuration.");
            return Collections.emptyIterator();
        }
        List<String> reportTypes = ReportUtils.getReportsTypeQuery(reportsQuery);
        log.debug("[QUERY] Kudu RECO Participants | Account: {} | Types: {}", participantProperties.getAccountId(), reportTypes);
        long startTime = System.currentTimeMillis();
        Iterator<ParticipantDTO> participantsFound = new PagedIterator<>(pageable ->
            reportingFileKudu.findParticipantsRecoFileType(startDate, finalDate, reportTypes,
                participantProperties.getAccountId(), pageable), STREAM_PAGE_SIZE);
        return LazyIterators.filterMap(participantsFound, participant -> {
            final String originalType = participant.getFileType();
            Optional<TranslationData> translation = reportProperties.getTranslation().getReports().stream()
                .filter(t -> t.getName().equals(originalType))
                .findFirst();

            if (translation.isPresent()) {
                String newValue = translation.get().getValue();
                participant.setFileType(newValue);
                log.trace("[TRANSLATE] {} -> {}", originalType, newValue);
            }
            return Optional.of(participant);
        });
    }

    public Iterator<RegulatorDTO> iterateRegulatorByDayAccountAndFileType(final String initDate, final String endDate) {
        log.debug("[START] findRegulatorByDayAccountAndFileType | Range: [{} - {}]", initDate, endDate);

        final LocalDateTime startDate = LocalDate.parse(initDate).atStartOfDay();
        final LocalDateTime finalDate = LocalDate.parse(endDate).atStartOfDay();
        log.debug("[PROCESS] | Range: [{} - {}]", startDate, finalDate);
        List<String> reportTypes = ReportUtils.getReportsTypeQuery(regulatorProperties.getReports());
        log.debug("[QUERY] Kudu Regulator | Account: {} | Types: {}", regulatorProperties.getAccountId(), reportTypes);
        return new PagedIterator<>(pageable -> reportingFileKudu.findRegulatorByDayAccountAndFileType(
            startDate, finalDate, reportTypes, regulatorProperties.getAccountId(), pageable), STREAM_PAGE_SIZE);
    }

    public Iterator<TrDTO> iterateTrByDayAccountAndFileType(final String initDate, final String endDate) {
        log.debug("[START] findTrByDayAccountAndFileType | Range: [{} - {}]", initDate, endDate);

        final LocalDateTime startDate = LocalDate.parse(initDate).atStartOfDay();
        final LocalDateTime finalDate = LocalDate.parse(endDate).atStartOfDay();

        List<String> reportTypes = ReportUtils.getReportsTypeQuery(trProperties.getReports());
        log.debug("[QUERY] Kudu TR | Account: {} | Types: {}", trProperties.getAccountId(), reportTypes);
        return new PagedIterator<>(pageable -> reportingFileKudu.findTrByDayAccountAndFileType(
            startDate, finalDate, reportTypes, trProperties.getAccountId(), pageable), STREAM_PAGE_SIZE);
    }

    @Override
    public List<ParticipantDTO> findParticipantsByDayAccountAndFileType(String initDate, String endDate) {
        return collect(iterateParticipantsByDayAccountAndFileType(initDate, endDate));
    }

    @Override
    public List<ParticipantDTO> findParticipantsRecoFileType(String initDate, String endDate) {
        return collect(iterateParticipantsRecoFileType(initDate, endDate));
    }

    @Override
    public List<RegulatorDTO> findRegulatorByDayAccountAndFileType(String initDate, String endDate) {
        return collect(iterateRegulatorByDayAccountAndFileType(initDate, endDate));
    }

    @Override
    public List<TrDTO> findTrByDayAccountAndFileType(String initDate, String endDate) {
        return collect(iterateTrByDayAccountAndFileType(initDate, endDate));
    }

    private <T> List<T> collect(Iterator<T> records) {
        List<T> results = new ArrayList<>();
        records.forEachRemaining(results::add);
        return results;
    }
}
