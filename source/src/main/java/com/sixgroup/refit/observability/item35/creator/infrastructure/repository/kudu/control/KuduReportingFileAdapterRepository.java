package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.kudu.control;

import com.sixgroup.refit.observability.item35.creator.configuration.*;
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
    private final ParticipantProperties participantProperties;
    private final RegulatorProperties regulatorProperties;
    private final TrProperties trProperties;
    private final ReportItemProperties reportProperties;

    public List<ParticipantDTO> findParticipantsByDayAccountAndFileType(final String initDate, final String endDate) {
        log.info("[START] findParticipantsByDayAccountAndFileType | Range: [{} - {}]", initDate, endDate);
        final LocalDateTime startDate = LocalDate.parse(initDate).atStartOfDay();
        final LocalDateTime finalDate = LocalDate.parse(endDate).atStartOfDay();
        log.info("[PROCESS] | Range: [{} - {}]", startDate, finalDate);
        final List<ReportConfig> reports = participantProperties.getReports();
        log.info("[REPORTS] | reports: [{}]", reports);
        final List<String> reportsCustom = participantProperties.getReportsCustom().stream().map(ReportConfig::getName).toList();
        log.info("[REPORT_CUSTOM] | reportsCustom: [{}]", reportsCustom);
        log.info("[CONFIG] Total reports in properties: {}. Custom reports to exclude: {}", reports.size(), reportsCustom.size());
        final List<ReportConfig> reportsQuery = reports.stream()
            .filter(report -> {
                boolean isNotCustom = !reportsCustom.contains(report.getName());
                if (!isNotCustom) {
                    log.trace("[FILTER] Excluding custom report: {}", report.getName());
                }
                return isNotCustom;
            })
            .toList();
        log.info("[REPORTSQUERY] | reportsQuery: [{}]", reportsQuery);
        if (reportsQuery.isEmpty()) {
            log.info("[SKIP] No standard reports found after filtering. Returning empty list.");
            return new ArrayList<>();
        }
        List<String> reportTypes = ReportUtils.getReportsTypeQuery(reportsQuery);
        log.info("[REPORT_TYPES] | reportTypes: [{}]", reportTypes);
        long startTime = System.currentTimeMillis();
        log.info("[START_TIME] | start_time: [{}]", startTime);
        log.info("[ACCOUNT_ID] | participantProperties.getAccountId(): [{}]", participantProperties.getAccountId());
        List<ParticipantDTO> result = reportingFileKudu.findParticipantsByDayAccountAndFileType(startDate, finalDate, reportTypes, participantProperties.getAccountId());
        log.info("[RESULT] | result: [{}]", result);
        long duration = System.currentTimeMillis() - startTime;
        log.info("[DURATION] | duration: [{}]", duration);
        return result;
    }

    public List<ParticipantDTO> findParticipantsRecoFileType(final String initDate, final String endDate) {
        log.info("[START] findParticipantsRecoFileType | Range: [{} - {}]", initDate, endDate);
        final LocalDateTime startDate = LocalDate.parse(initDate).atStartOfDay();
        final LocalDateTime finalDate = LocalDate.parse(endDate).atStartOfDay();
        log.info("[PROCESS] | Range: [{} - {}]", startDate, finalDate);
        final List<ReportConfig> reports = participantProperties.getReports();
        log.info("[REPORTS] | reports: [{}]", reports);
        final List<String> reportsCustomNames = participantProperties.getReportsCustom().stream().map(ReportConfig::getName).toList();
        log.info("[REPORT_CUSTOM_NAMES] | reportsCustomNames: [{}]", reportsCustomNames);
        final List<ReportConfig> reportsQuery = reports.stream().filter(report -> reportsCustomNames.contains(report.getName())).toList();
        log.info("[REPORTSQUERY] | reportsQuery: [{}]", reportsQuery);
        if (reportsQuery.isEmpty()) {
            log.info("[SKIP] No custom/RECO reports found for query. Check configuration.");
            return new ArrayList<>();
        }
        List<String> reportTypes = ReportUtils.getReportsTypeQuery(reportsQuery);
        log.info("[QUERY] Kudu RECO Participants | Account: {} | Types: {}", participantProperties.getAccountId(), reportTypes);
        long startTime = System.currentTimeMillis();
        final List<ParticipantDTO> participantsFound = reportingFileKudu.findParticipantsRecoFileType(startDate, finalDate, reportTypes, participantProperties.getAccountId());
        log.info("[PARTICIPANTSFOUND] | participantsFound: [{}]", reportsQuery);
        long duration = System.currentTimeMillis() - startTime;
        log.info("[DURATION] | duration: [{}]", duration);
        if (participantsFound.isEmpty()) {
            log.info("[END] No RECO participants found in Kudu | Time: {}ms", duration);
            return new ArrayList<>();
        }
        log.info("[PROCESS] Translating {} participants...", participantsFound.size());
        int translatedCount = 0;
        for (ParticipantDTO participant : participantsFound) {
            final String originalType = participant.getFileType();
            Optional<TranslationData> translation = reportProperties.getTranslation().getReports().stream()
                .filter(t -> t.getName().equals(originalType))
                .findFirst();

            if (translation.isPresent()) {
                String newValue = translation.get().getValue();
                participant.setFileType(newValue);
                log.trace("[TRANSLATE] {} -> {}", originalType, newValue);
                translatedCount++;
            }
        }

        log.info("[END] RECO finished | Total: {} | Translated: {} | Kudu Time: {}ms",
            participantsFound.size(), translatedCount, duration);
        return participantsFound;
    }

    public List<RegulatorDTO> findRegulatorByDayAccountAndFileType(final String initDate, final String endDate) {
        log.info("[START] findRegulatorByDayAccountAndFileType | Range: [{} - {}]", initDate, endDate);

        final LocalDateTime startDate = LocalDate.parse(initDate).atStartOfDay();
        final LocalDateTime finalDate = LocalDate.parse(endDate).atStartOfDay();
        log.info("[PROCESS] | Range: [{} - {}]", startDate, finalDate);
        List<String> reportTypes = ReportUtils.getReportsTypeQuery(regulatorProperties.getReports());
        log.info("[QUERY] Kudu Regulator | Account: {} | Types: {}", regulatorProperties.getAccountId(), reportTypes);
        long startTime = System.currentTimeMillis();
        List<RegulatorDTO> result = reportingFileKudu.findRegulatorByDayAccountAndFileType(startDate, finalDate, reportTypes, regulatorProperties.getAccountId());
        long duration = System.currentTimeMillis() - startTime;

        log.info("[END] Regulator records found: {} | Time: {}ms", result.size(), duration);
        return result;
    }

    public List<TrDTO> findTrByDayAccountAndFileType(final String initDate, final String endDate) {
        log.info("[START] findTrByDayAccountAndFileType | Range: [{} - {}]", initDate, endDate);

        final LocalDateTime startDate = LocalDate.parse(initDate).atStartOfDay();
        final LocalDateTime finalDate = LocalDate.parse(endDate).atStartOfDay();

        List<String> reportTypes = ReportUtils.getReportsTypeQuery(trProperties.getReports());
        log.info("[QUERY] Kudu TR | Account: {} | Types: {}", trProperties.getAccountId(), reportTypes);
        long startTime = System.currentTimeMillis();
        List<TrDTO> result = reportingFileKudu.findTrByDayAccountAndFileType(startDate, finalDate,
            reportTypes, trProperties.getAccountId());
        long duration = System.currentTimeMillis() - startTime;

        log.info("[END] TR records found: {} | Time: {}ms", result.size(), duration);
        return result;
    }
}
