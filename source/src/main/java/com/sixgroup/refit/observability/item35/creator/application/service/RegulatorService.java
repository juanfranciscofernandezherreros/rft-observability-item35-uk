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
import com.sixgroup.refit.observability.item35.creator.shared.utils.LazyIterators;
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

    public Iterator<ReportGenerationDto> iterateRegulator(final String initDate, final String endDate,
                                                           final String itemDate) {
        Iterator<RegulatorDTO> regulations = reportingFileAdapterRepository
            .iterateRegulatorByDayAccountAndFileType(initDate, endDate);
        Map<String, ReguIdentityDTO> traceCodeRegulatorMap = buildRegulatorMap(Collections.emptyList());
        final List<ReportEoDDTO> reportsEoD = reportEodProcessStateRepository.find(initDate, endDate);

        return LazyIterators.filterMap(regulations, regulator -> {
            if (!traceCodeRegulatorMap.containsKey(regulator.getAccountTrace())) {
                List<ReguIdentityDTO> identities = fetchAllReguIdentityEntities(List.of(regulator.getAccountTrace()));
                identities.forEach(identity -> traceCodeRegulatorMap.putIfAbsent(identity.getTraceCode(), identity));
            }
            if (!traceCodeRegulatorMap.containsKey(regulator.getAccountTrace())) {
                return Optional.empty();
            }

            final Optional<ReportEoDDTO> reportEoDFound = findReportEod(reportsEoD, fileTypeProperties.getReports(), regulator.getFileType(), regulator.getReportingSession());
            Optional<SlaInfo> slaInfo;
            if (reportEoDFound.isPresent()) {
                slaInfo = slaInfoRepository.getSlaInfo(REGULATOR_ENTITY, regulator.getFileType(), regulator.getReportingSession(), reportEoDFound.get().getStartedDate(), regulator.getCreationDate());
            } else {
                slaInfo = slaInfoRepository.getSlaInfo(REGULATOR_ENTITY, regulator.getFileType(), regulator.getReportingSession(), regulator.getCreationDate());
            }

            if (slaInfo.isEmpty()) {
                log.error("[CRITICAL ERROR] SlaInfo NOT found. Details: Entity={}, FileType={}, Session={}, Date={}",
                    REGULATOR_ENTITY, regulator.getFileType(), regulator.getReportingSession(), regulator.getCreationDate());
                return Optional.empty();
            }

            ReportGenerationDto result = regulatorMapper.toReportGenerationDto(
                regulator, fileTypeProperties, slaInfo.get(), traceCodeRegulatorMap);
            result.setReportingDate(DateUtils.itemDateFormatted(itemDate));
            return Optional.of(result);
        });
    }

    public List<ReportGenerationDto> findRegulator(final String initDate, final String endDate,
                                                    final String itemDate) {
        List<ReportGenerationDto> results = new ArrayList<>();
        iterateRegulator(initDate, endDate, itemDate).forEachRemaining(results::add);
        return results;
    }

    protected Optional<ReportEoDDTO> findReportEod(final List<ReportEoDDTO> reportsEoD,
                                                   final List<ReportConfig> reports,
                                                   final String fileType,
                                                   final LocalDateTime reportingSession) {
        log.debug("[HELPER] Searching ReportEod match for FileType: {}", fileType);

        if (reportsEoD.isEmpty()) {
            log.debug("[HELPER] ReportEod list is empty, skipping search.");
            return Optional.empty();
        }

        final Optional<ReportConfig> reportConfigFound = reports.stream()
            .filter(report -> fileType.equals(report.getReportName()))
            .findFirst();

        if (reportConfigFound.isEmpty()) {
            log.debug("[HELPER] No matching ReportConfig found for name: {}", fileType);
            return Optional.empty();
        }

        final String reportQueryEodQuery = reportConfigFound.get().getReportQueryEod();
        final String reportingSessionQuery = DateUtils.localDateTimeToSpainDateFormat(reportingSession);

        log.debug("[HELPER] Comparing with EOD state using QueryType: '{}' and SessionDate: '{}'", reportQueryEodQuery, reportingSessionQuery);

        return reportsEoD.stream()
            .filter(report -> reportQueryEodQuery.equals(report.getReportType()) && reportingSessionQuery.equals(report.getReportingSession()))
            .findFirst();
    }

    private Map<String, ReguIdentityDTO> buildRegulatorMap(final List<ReguIdentityDTO> reguIdentityEntities) {
        final Map<String, ReguIdentityDTO> map = new HashMap<>();

        log.debug("[MAP BUILDER] Adding {} translation accounts from configuration properties", reportProperties.getTranslation().getAccounts().size());
        for (TranslationData account : reportProperties.getTranslation().getAccounts()) {
            map.put(account.name, ReguIdentityDTO.builder()
                .regulatorId(account.name)
                .traceCode(account.getValue())
                .traceConnectivity(false)
                .isTranslatedAccount(true)
                .build());
        }

        log.debug("[MAP BUILDER] Merging {} identities from repository into map (Avoiding duplicates)", reguIdentityEntities.size());
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

        log.debug("[BATCH FETCH] Starting identity fetch. Total traces: {}, Block size: {}, Total partitions: {}",
            accountTraces.size(), blockSize, partitionedAccountTraces.size());

        for (int i = 0; i < partitionedAccountTraces.size(); i++) {
            List<String> partition = partitionedAccountTraces.get(i);
            log.debug("[BATCH FETCH] Querying partition {}/{} with {} items", i + 1, partitionedAccountTraces.size(), partition.size());

            final List<ReguIdentityDTO> reguIdentityEntities = reguIdentityAdapterRepository.findByTraceCode(partition);
            log.debug("[BATCH FETCH] Result: Partition {} returned {} records", i + 1, reguIdentityEntities.size());

            definitiveList.addAll(reguIdentityEntities);
        }

        return definitiveList;
    }

    private void printTraceCodeRegulatorId(final List<ReguIdentityDTO> definitiveList) {
        log.debug("[DEBUG] Full ReguIdentity list (JSON): {}", new Gson().toJson(definitiveList));
    }
}
