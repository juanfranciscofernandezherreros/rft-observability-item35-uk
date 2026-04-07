package com.sixgroup.refit.observability.item35.creator.application.service;

import com.google.gson.Gson;
import com.sixgroup.refit.observability.item35.creator.configuration.RegulatorProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.ReportItemProperties;
import com.sixgroup.refit.observability.item35.creator.domain.config.ReportConfig;
import com.sixgroup.refit.observability.item35.creator.domain.config.TranslationData;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReguIdentityDTO;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.domain.repository.control.ReportingFileAdapterRepository;
import com.sixgroup.refit.observability.item35.creator.domain.repository.reportstate.ReportEodProcessStateRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.RegulatorDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.sqlserver.ReportEoDDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.mappper.RegulatorMapper;
import com.sixgroup.refit.observability.item35.creator.infrastructure.repository.kudu.account.ReguIdentityAdapterRepository;
import com.sixgroup.refit.observability.item35.creator.shared.sla.SlaInfoRepository;
import com.sixgroup.refit.observability.item35.creator.shared.utils.DateUtils;
import com.sixgroup.refit.observability.modules.validate.domain.data.SlaInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.AppConstants.REGULATOR_ENTITY;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegulatorService {

    private final ReportingFileAdapterRepository reportingFileAdapterRepository;
    private final RegulatorProperties fileTypeProperties;
    private final ReportItemProperties reportProperties;
    private final SlaInfoRepository slaInfoRepository;
    private final RegulatorMapper regulatorMapper = new RegulatorMapper();
    private final ReguIdentityAdapterRepository reguIdentityAdapterRepository;
    private final ReportEodProcessStateRepository reportEodProcessStateRepository;

    @Value("${component-config.kududb-account.blockSize}")
    private int blockSize;

    public List<ReportGenerationDto> findRegulator(final String initDate, final String endDate, final String itemDate) {
        log.info("[START] Entering findRegulator method. Parameters: initDate={}, endDate={}, itemDate={}", initDate, endDate, itemDate);

        // 1. Fetching Regulation records
        log.info("[QUERY] Fetching regulators from Kudu via findRegulatorByDayAccountAndFileType...");
        final List<RegulatorDTO> regulations = reportingFileAdapterRepository.findRegulatorByDayAccountAndFileType(initDate, endDate);
        log.info("[QUERY RESULT] {} regulation records retrieved for period {} to {}", regulations.size(), initDate, endDate);

        // 2. Extracting traces for identity lookup
        final List<String> accountTraces = regulations.stream().map(RegulatorDTO::getAccountTrace).distinct().toList();
        log.info("[DATA PREPARATION] Extracted {} unique account traces from regulations", accountTraces.size());

        // 3. Batch fetch identities
        final List<ReguIdentityDTO> reguIdentities = fetchAllReguIdentityEntities(accountTraces);
        log.info("[DATA PREPARATION] Total ReguIdentity entities resolved: {}", reguIdentities.size());

        // 4. Initial validation
        if (regulations.isEmpty() || reguIdentities.isEmpty()) {
            log.info("[STOP] Terminating process: Regulations List Empty: {}, ReguIdentities List Empty: {}",
                regulations.isEmpty(), reguIdentities.isEmpty());
            return new ArrayList<>();
        }

        // 5. Building the cross-reference map
        log.info("[PROCESS] Building trace-to-identity mapping including translation accounts...");
        final Map<String, ReguIdentityDTO> traceCodeRegulatorMap = buildRegulatorMap(reguIdentities);
        log.info("[PROCESS] Map construction complete. Final map size: {}", traceCodeRegulatorMap.size());

        printTraceCodeRegulatorId(reguIdentities);

        // 6. Fetching EOD State
        log.info("[QUERY] Fetching Report EOD status from SQL Server for range {} to {}...", initDate, endDate);
        final List<ReportEoDDTO> reportsEoD = reportEodProcessStateRepository.find(initDate, endDate);
        log.info("[QUERY RESULT] Found {} ReportEoD entries in the state repository", reportsEoD.size());

        final List<ReportGenerationDto> regulatorReportGenerationData = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(1);
        int totalToProcess = regulations.size();

        // 7. Processing each regulation
        regulations.forEach(regulator -> {
            int idx = counter.getAndIncrement();
            log.info("[ITERATION {}/{}] Processing Regulator: FileType={}, Session={}, Trace={}",
                idx, totalToProcess, regulator.getFileType(), regulator.getReportingSession(), regulator.getAccountTrace());

            Optional<SlaInfo> slaInfo;
            log.info("[SLA LOOKUP] Attempting to match ReportEoD for FileType: '{}'", regulator.getFileType());
            final Optional<ReportEoDDTO> reportEoDFound = findReportEod(reportsEoD, fileTypeProperties.getReports(), regulator.getFileType(), regulator.getReportingSession());

            if (reportEoDFound.isPresent()) {
                log.info("[SLA CONTEXT] Match found in EOD Table. Using StartedDate: {} and CreationDate: {}",
                    reportEoDFound.get().getStartedDate(), regulator.getCreationDate());
                slaInfo = slaInfoRepository.getSlaInfo(REGULATOR_ENTITY, regulator.getFileType(), regulator.getReportingSession(), reportEoDFound.get().getStartedDate(), regulator.getCreationDate());
            } else {
                log.info("[SLA CONTEXT] No EOD match. Falling back to default SLA lookup with CreationDate: {}", regulator.getCreationDate());
                slaInfo = slaInfoRepository.getSlaInfo(REGULATOR_ENTITY, regulator.getFileType(), regulator.getReportingSession(), regulator.getCreationDate());
            }

            if (slaInfo.isEmpty()) {
                log.error("[CRITICAL ERROR] SlaInfo NOT found. Details: Entity={}, FileType={}, Session={}, Date={}",
                    REGULATOR_ENTITY, regulator.getFileType(), regulator.getReportingSession(), regulator.getCreationDate());
            } else {
                log.info("[SUCCESS] SLA details found: {}. Proceeding with mapping...", slaInfo.get());

                final ReportGenerationDto reportGenerationDto = regulatorMapper.toReportGenerationDto(regulator, fileTypeProperties, slaInfo.get(), traceCodeRegulatorMap);

                String formattedDate = DateUtils.itemDateFormatted(itemDate);
                reportGenerationDto.setReportingDate(formattedDate);
                log.info("[MAPPING] Created DTO for {}. ReportingDate set to {}", regulator.getFileType(), formattedDate);

                regulatorReportGenerationData.add(reportGenerationDto);
            }
        });

        log.info("[FINISH] Regulator process ended. Successfully generated {}/{} report DTOs",
            regulatorReportGenerationData.size(), totalToProcess);
        return regulatorReportGenerationData;
    }

    protected Optional<ReportEoDDTO> findReportEod(final List<ReportEoDDTO> reportsEoD,
                                                   final List<ReportConfig> reports,
                                                   final String fileType,
                                                   final LocalDateTime reportingSession) {
        log.info("[HELPER] Searching ReportEod match for FileType: {}", fileType);

        if (reportsEoD.isEmpty()) {
            log.info("[HELPER] ReportEod list is empty, skipping search.");
            return Optional.empty();
        }

        final Optional<ReportConfig> reportConfigFound = reports.stream()
            .filter(report -> fileType.equals(report.getReportName()))
            .findFirst();

        if (reportConfigFound.isEmpty()) {
            log.info("[HELPER] No matching ReportConfig found for name: {}", fileType);
            return Optional.empty();
        }

        final String reportQueryEodQuery = reportConfigFound.get().getReportQueryEod();
        final String reportingSessionQuery = DateUtils.localDateTimeToSpainDateFormat(reportingSession);

        log.info("[HELPER] Comparing with EOD state using QueryType: '{}' and SessionDate: '{}'", reportQueryEodQuery, reportingSessionQuery);

        return reportsEoD.stream()
            .filter(report -> reportQueryEodQuery.equals(report.getReportType()) && reportingSessionQuery.equals(report.getReportingSession()))
            .findFirst();
    }

    private Map<String, ReguIdentityDTO> buildRegulatorMap(final List<ReguIdentityDTO> reguIdentityEntities) {
        final Map<String, ReguIdentityDTO> map = new HashMap<>();

        log.info("[MAP BUILDER] Adding {} translation accounts from configuration properties", reportProperties.getTranslation().getAccounts().size());
        for (TranslationData account : reportProperties.getTranslation().getAccounts()) {
            map.put(account.name, ReguIdentityDTO.builder()
                .regulatorId(account.name)
                .traceCode(account.getValue())
                .traceConnectivity(false)
                .isTranslatedAccount(true)
                .build());
        }

        log.info("[MAP BUILDER] Merging {} identities from repository into map (Avoiding duplicates)", reguIdentityEntities.size());
        for (ReguIdentityDTO dto : reguIdentityEntities) {
            if (!map.containsKey(dto.getTraceCode())) {
                map.put(dto.getTraceCode(), dto);
            }
        }

        return map;
    }

    private List<ReguIdentityDTO> fetchAllReguIdentityEntities(final List<String> accountTraces) {
        final List<ReguIdentityDTO> definitiveList = new ArrayList<>();
        final List<List<String>> partitionedAccountTraces = ListUtils.partition(accountTraces, blockSize);

        log.info("[BATCH FETCH] Starting identity fetch. Total traces: {}, Block size: {}, Total partitions: {}",
            accountTraces.size(), blockSize, partitionedAccountTraces.size());

        for (int i = 0; i < partitionedAccountTraces.size(); i++) {
            List<String> partition = partitionedAccountTraces.get(i);
            log.info("[BATCH FETCH] Querying partition {}/{} with {} items", i + 1, partitionedAccountTraces.size(), partition.size());

            final List<ReguIdentityDTO> reguIdentityEntities = reguIdentityAdapterRepository.findByTraceCode(partition);
            log.info("[BATCH FETCH] Result: Partition {} returned {} records", i + 1, reguIdentityEntities.size());

            definitiveList.addAll(reguIdentityEntities);
        }

        return definitiveList;
    }

    private void printTraceCodeRegulatorId(final List<ReguIdentityDTO> definitiveList) {
        log.info("[DEBUG] Full ReguIdentity list (JSON): {}", new Gson().toJson(definitiveList));
    }
}
