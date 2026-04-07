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
        log.info("[START] findParticipantsByDayAccountAndFileType execution with dates: [{} to {}]", initDate, endDate);

        final LocalDateTime startDate = LocalDate.parse(initDate).atStartOfDay();
        final LocalDateTime finalDate = LocalDate.parse(endDate).atStartOfDay();
        log.debug("[PROCESS] Dates successfully converted to LocalDateTime: {} - {}", startDate, finalDate);
        final List<ReportConfig> reports = participantProperties.getReports();
        log.info("[CONFIG] Full reports list (names): {}", reports.stream().map(ReportConfig::getName).toList());
        final List<String> reportsCustom = participantProperties.getReportsCustom().stream()
            .map(ReportConfig::getName)
            .toList();
        log.info("[CONFIG] Custom reports list to exclude: {}", reportsCustom);
        log.info("[CONFIG] Properties loaded. Total reports available: {}. Custom reports to exclude: {}", reports.size(), reportsCustom);

        final List<ReportConfig> reportsQuery = reports.stream()
            .filter(report -> {
                boolean isNotCustom = !reportsCustom.contains(report.getName());
                if (!isNotCustom) {
                    log.debug("[FILTER] Excluding custom report from standard query: {}", report.getName());
                }
                return isNotCustom;
            })
            .toList();

        if (reportsQuery.isEmpty()) {
            log.warn("[SKIP] The filtered reports list is empty. No query will be executed for standard participants.");
            return new ArrayList<>();
        }

        List<String> reportTypes = ReportUtils.getReportsTypeQuery(reportsQuery);
        log.info("[QUERY] Requesting standard participants from Kudu. AccountId: {}, ReportTypes: {}",
            participantProperties.getAccountId(), reportTypes);

        List<ParticipantDTO> result = reportingFileKudu.findParticipantsByDayAccountAndFileType(startDate, finalDate,
            reportTypes, participantProperties.getAccountId());

        log.info("[END] Query finished. Total standard participants retrieved: {}", result.size());
        return result;
    }

    public List<ParticipantDTO> findParticipantsRecoFileType(final String initDate, final String endDate) {
        log.info("[START] findParticipantsRecoFileType execution with dates: [{} to {}]", initDate, endDate);

        final LocalDateTime startDate = LocalDate.parse(initDate).atStartOfDay();
        final LocalDateTime finalDate = LocalDate.parse(endDate).atStartOfDay();

        final List<ReportConfig> reports = participantProperties.getReports();
        final List<String> reportsCustom = participantProperties.getReportsCustom().stream()
            .map(ReportConfig::getName)
            .toList();

        final List<ReportConfig> reportsQuery = reports.stream()
            .filter(report -> {
                boolean isCustom = reportsCustom.contains(report.getName());
                if (isCustom) {
                    log.debug("[FILTER] Including custom report for RECO query: {}", report.getName());
                }
                return isCustom;
            })
            .toList();

        if (reportsQuery.isEmpty()) {
            log.warn("[SKIP] No custom/RECO reports found in configuration. Aborting query.");
            return new ArrayList<>();
        }

        List<String> reportTypes = ReportUtils.getReportsTypeQuery(reportsQuery);
        log.info("[QUERY] Requesting RECO participants from Kudu for types: {}", reportTypes);

        final List<ParticipantDTO> participantsFound = reportingFileKudu.findParticipantsRecoFileType(startDate, finalDate,
            reportTypes, participantProperties.getAccountId());

        if (participantsFound.isEmpty()) {
            log.info("[END] No RECO participants found for the given criteria.");
            return new ArrayList<>();
        }

        log.info("[LIST PROCESSING] Starting translation for {} retrieved participants", participantsFound.size());

        int translatedCount = 0;
        for (ParticipantDTO participant : participantsFound) {
            final String originalType = participant.getFileType();
            final Optional<TranslationData> translationFound = reportProperties.getTranslation().getReports().stream()
                .filter(report -> report.getName().equals(originalType))
                .findFirst();

            if (translationFound.isPresent()) {
                String newValue = translationFound.get().getValue();
                participant.setFileType(newValue);
                log.debug("[TRANSLATE] Item updated: [{}] -> [{}]", originalType, newValue);
                translatedCount++;
            } else {
                log.trace("[TRANSLATE] No translation rule found for type: {}. Keeping original.", originalType);
            }
        }

        log.info("[END] RECO processing finished. Items processed: {}. Items translated: {}",
            participantsFound.size(), translatedCount);
        return participantsFound;
    }

    public List<RegulatorDTO> findRegulatorByDayAccountAndFileType(final String initDate, final String endDate) {
        log.info("[START] findRegulatorByDayAccountAndFileType - Range: [{} to {}]", initDate, endDate);

        final LocalDateTime startDate = LocalDate.parse(initDate).atStartOfDay();
        final LocalDateTime finalDate = LocalDate.parse(endDate).atStartOfDay();

        List<String> reportTypes = ReportUtils.getReportsTypeQuery(regulatorProperties.getReports());
        log.info("[QUERY] Executing Regulator query. Account: {}, Types: {}",
            regulatorProperties.getAccountId(), reportTypes);

        List<RegulatorDTO> result = reportingFileKudu.findRegulatorByDayAccountAndFileType(startDate, finalDate,
            reportTypes, regulatorProperties.getAccountId());

        log.info("[END] Regulator query completed. Records found: {}", result.size());
        return result;
    }

    public List<TrDTO> findTrByDayAccountAndFileType(final String initDate, final String endDate) {
        log.info("[START] findTrByDayAccountAndFileType - Range: [{} to {}]", initDate, endDate);

        final LocalDateTime startDate = LocalDate.parse(initDate).atStartOfDay();
        final LocalDateTime finalDate = LocalDate.parse(endDate).atStartOfDay();

        List<String> reportTypes = ReportUtils.getReportsTypeQuery(trProperties.getReports());
        log.info("[QUERY] Executing TR query. Account: {}, Types: {}",
            trProperties.getAccountId(), reportTypes);

        List<TrDTO> result = reportingFileKudu.findTrByDayAccountAndFileType(startDate, finalDate,
            reportTypes, trProperties.getAccountId());

        log.info("[END] TR query completed. Records found: {}", result.size());
        return result;
    }
}
