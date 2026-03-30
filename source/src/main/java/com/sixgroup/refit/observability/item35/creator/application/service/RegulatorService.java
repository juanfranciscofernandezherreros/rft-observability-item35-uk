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
        final List<RegulatorDTO> regulations = reportingFileAdapterRepository.findRegulatorByDayAccountAndFileType(initDate, endDate);
        log.debug("Query 'findRegulatorByDayAccountAndFileType' finished. Records found: {} for range {} to {}",
            regulations.size(), initDate, endDate);

        final List<String> accountTraces = regulations.stream().map(RegulatorDTO::getAccountTrace).distinct().toList();

        final List<ReguIdentityDTO> reguIdentities = fetchAllReguIdentityEntities(accountTraces);
        log.debug("Successfully fetched a total of {} ReguIdentity entities", reguIdentities.size());

        if (regulations.isEmpty() || reguIdentities.isEmpty()) {
            log.debug("Process stopped: Regulations empty ({}) or ReguIdentities empty ({})",
                regulations.isEmpty(), reguIdentities.isEmpty());
            return new ArrayList<>();
        }

        final Map<String, ReguIdentityDTO> traceCodeRegulatorMap = buildRegulatorMap(reguIdentities);
        printTraceCodeRegulatorId(reguIdentities);

        final List<ReportEoDDTO> reportsEoD = reportEodProcessStateRepository.find(initDate, endDate);
        log.debug("Query 'reportEodProcessStateRepository.find' finished. Retrieved {} ReportEoD records", reportsEoD.size());

        final List<ReportGenerationDto> regulatorReportGenerationData = new ArrayList<>();
        regulations.forEach(regulator -> {
            Optional<SlaInfo> slaInfo;
            final Optional<ReportEoDDTO> reportEoDFound = findReportEod(reportsEoD, fileTypeProperties.getReports(), regulator.getFileType(), regulator.getReportingSession());

            if (reportEoDFound.isPresent()) {
                slaInfo = slaInfoRepository.getSlaInfo(REGULATOR_ENTITY, regulator.getFileType(), regulator.getReportingSession(), reportEoDFound.get().getStartedDate(), regulator.getCreationDate());
            } else {
                slaInfo = slaInfoRepository.getSlaInfo(REGULATOR_ENTITY, regulator.getFileType(), regulator.getReportingSession(), regulator.getCreationDate());
            }

            if (slaInfo.isEmpty()) {
                log.error("Failed to find SlaInfo for entity: {}, fileType: {}, session: {}, creationDate: {}. Check configuration.",
                    REGULATOR_ENTITY, regulator.getFileType(), regulator.getReportingSession(), regulator.getCreationDate());
            } else {
                log.debug("SlaInfo retrieved successfully for FileType: {} and Session: {}",
                    regulator.getFileType(), regulator.getReportingSession());

                final ReportGenerationDto reportGenerationDto = regulatorMapper.toReportGenerationDto(regulator, fileTypeProperties, slaInfo.get(), traceCodeRegulatorMap);
                reportGenerationDto.setReportingDate(DateUtils.itemDateFormatted(itemDate));
                regulatorReportGenerationData.add(reportGenerationDto);
            }
        });

        log.debug("Regulator processing completed. Total ReportGenerationDto objects created: {}", regulatorReportGenerationData.size());
        return regulatorReportGenerationData;
    }

    protected Optional<ReportEoDDTO> findReportEod(final List<ReportEoDDTO> reportsEoD,
                                                   final List<ReportConfig> reports,
                                                   final String fileType,
                                                   final LocalDateTime reportingSession) {
        if (reportsEoD.isEmpty()) {
            return Optional.empty();
        }

        final Optional<ReportConfig> reportConfigFound = reports.stream().filter(report -> fileType.equals(report.getReportName())).findFirst();
        if (reportConfigFound.isEmpty()) {
            return Optional.empty();
        }
        final String reportQueryEodQuery = reportConfigFound.get().getReportQueryEod();
        final String reportingSessionQuery = DateUtils.localDateTimeToSpainDateFormat(reportingSession);

        return reportsEoD.stream().filter(report -> reportQueryEodQuery.equals(report.getReportType()) && reportingSessionQuery.equals(report.getReportingSession())).findFirst();
    }

    private Map<String, ReguIdentityDTO> buildRegulatorMap(final List<ReguIdentityDTO> reguIdentityEntities) {
        final Map<String, ReguIdentityDTO> map = new HashMap<>();
        for (TranslationData account : reportProperties.getTranslation().getAccounts()) {
            map.put(account.name, ReguIdentityDTO.builder()
                .regulatorId(account.name)
                .traceCode(account.getValue())
                .traceConnectivity(false)
                .isTranslatedAccount(true)
                .build());
        }

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

        log.debug("Starting batch processing for {} account traces with block size {}", accountTraces.size(), blockSize);

        partitionedAccountTraces.forEach(partition -> {
            final List<ReguIdentityDTO> reguIdentityEntities = reguIdentityAdapterRepository.findByTraceCode(partition);
            log.debug("Batch query 'findByTraceCode' found {} identities for partition size: {}",
                reguIdentityEntities.size(), partition.size());
            definitiveList.addAll(reguIdentityEntities);
        });

        return definitiveList;
    }

    private void printTraceCodeRegulatorId(final List<ReguIdentityDTO> definitiveList) {
        log.debug("Identity List JSON Trace: {}", new Gson().toJson(definitiveList));
    }

}
