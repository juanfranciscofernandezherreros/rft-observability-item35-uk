package com.sixgroup.refit.observability.item35.creator.application.service;

import com.google.gson.Gson;
import com.sixgroup.refit.observability.item35.creator.configuration.RegulatorFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.ReportProperties;
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
    private final RegulatorFileTypeProperties fileTypeProperties;
    private final ReportProperties reportProperties;
    private final SlaInfoRepository slaInfoRepository;
    private final RegulatorMapper regulatorMapper = new RegulatorMapper();
    private final ReguIdentityAdapterRepository reguIdentityAdapterRepository;
    private final ReportEodProcessStateRepository reportEodProcessStateRepository;

    @Value("${component-config.kududb-account.blockSize}")
    private int blockSize;

    public List<ReportGenerationDto> findRegulator(final String initDate, final String endDate, final String itemDate) {
        final List<RegulatorDTO> regulations = reportingFileAdapterRepository.findRegulatorByDayAccountAndFileType(initDate, endDate);
        final List<String> accountTraces = regulations.stream().map(RegulatorDTO::getAccountTrace).distinct().toList();

        final List<ReguIdentityDTO> reguIdentities = fetchAllReguIdentityEntities(accountTraces);
        if (regulations.isEmpty() || reguIdentities.isEmpty()) {
            return new ArrayList<>();
        }

        final Map<String, ReguIdentityDTO> traceCodeRegulatorMap = buildRegulatorMap(reguIdentities);
        printTraceCodeRegulatorId(reguIdentities);

        //Find start report type
        final List<ReportEoDDTO> reportsEoD = reportEodProcessStateRepository.find(initDate, endDate);

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
                log.error("Error to find SlaInfo with entity {}, reportName {}, reportSession {}, reportDate {}. Configure properties",
                    REGULATOR_ENTITY, regulator.getFileType(), regulator.getReportingSession(), regulator.getCreationDate());
            } else {
                final ReportGenerationDto reportGenerationDto = regulatorMapper.toReportGenerationDto(regulator, fileTypeProperties, slaInfo.get(), traceCodeRegulatorMap);
                reportGenerationDto.setReportingDate(DateUtils.itemDateFormatted(itemDate));
                regulatorReportGenerationData.add(reportGenerationDto);
            }
        });

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
        for (ReguIdentityDTO dto : reguIdentityEntities) {
            map.put(dto.getTraceCode(), dto);
        }

        //Add translation accounts
        for (TranslationData account : reportProperties.getTranslation().getAccounts()) {
            if (!map.containsKey(account.value)) {
                map.put(account.name, ReguIdentityDTO.builder().regulatorId(account.name).traceCode(account.getValue()).traceConnectivity(false).isTranslatedAccount(true).build());
            }
        }

        return map;
    }

    private List<ReguIdentityDTO> fetchAllReguIdentityEntities(final List<String> accountTraces) {
        final List<ReguIdentityDTO> definitiveList = new ArrayList<>();
        final List<List<String>> partitionedAccountTraces = ListUtils.partition(accountTraces, blockSize);

        partitionedAccountTraces.forEach(partition -> {
            final List<ReguIdentityDTO> reguIdentityEntities = reguIdentityAdapterRepository.findByTraceCode(partition);
            definitiveList.addAll(reguIdentityEntities);
        });

        return definitiveList;
    }

    private void printTraceCodeRegulatorId(final List<ReguIdentityDTO> definitiveList) {
        log.info("{}", new Gson().toJson(definitiveList));
    }

}
