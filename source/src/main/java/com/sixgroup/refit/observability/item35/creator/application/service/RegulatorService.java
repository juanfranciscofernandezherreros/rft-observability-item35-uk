package com.sixgroup.refit.observability.item35.creator.application.service;

import com.google.gson.Gson;
import com.sixgroup.refit.observability.item35.creator.configuration.RegulatorFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReguIdentityDTO;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.domain.repository.control.ReportingFileAdapterRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.RegulatorDTO;
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

import java.util.*;
import java.util.stream.Collectors;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.REGULATOR_ENTITY;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegulatorService {

    private final ReportingFileAdapterRepository reportingFileAdapterRepository;
    private final RegulatorFileTypeProperties fileTypeProperties;
    private final SlaInfoRepository slaInfoRepository;
    private final RegulatorMapper regulatorMapper = new RegulatorMapper();
    private final ReguIdentityAdapterRepository reguIdentityAdapterRepository;

    @Value("${component-config.kududb-account.blockSize}")
    private int blockSize;

    public List<ReportGenerationDto> findRegulator(final String initDate, final String endDate, final String itemDate) {
        final List<RegulatorDTO> regulations = reportingFileAdapterRepository.findRegulatorByDayAccountAndFileType(initDate, endDate);
        List<String> accountTraces = regulations.stream().map(RegulatorDTO::getAccountTrace).distinct().toList();
        List<ReguIdentityDTO> definitiveList = fetchAllReguIdentityEntities(accountTraces);
        if (regulations.isEmpty() || definitiveList.isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, String> traceCodeRegulatorMap = buildRegulatorMap(definitiveList);
        printTraceCodeRegulatorId(definitiveList);
        final List<ReportGenerationDto> regulatorReportGenerationData = new ArrayList<>();
        regulations.forEach(regulator -> {
            final Optional<SlaInfo> slaInfo = slaInfoRepository.getSlaInfo(REGULATOR_ENTITY, regulator.getFileType(), regulator.getReportingSession(), regulator.getCreationDate());
            if (slaInfo.isEmpty()) {
                log.error("Error to find SlaInfo with entity {}, reportName {}, reportSession {}, reportDate {}. Configure properties",
                    REGULATOR_ENTITY, regulator.getFileType(), regulator.getReportingSession(), regulator.getCreationDate());
            } else {
                ReportGenerationDto reportGenerationDto = regulatorMapper.toReportGenerationDto(regulator, fileTypeProperties, slaInfo.get(), traceCodeRegulatorMap);
                reportGenerationDto.setReportingDate(DateUtils.itemDateFormatted(itemDate));
                regulatorReportGenerationData.add(reportGenerationDto);
            }
        });

        return regulatorReportGenerationData;
    }

    private Map<String, String> buildRegulatorMap(List<ReguIdentityDTO> reguIdentityEntities) {
        return reguIdentityEntities.stream()
            .collect(Collectors.toMap(
                ReguIdentityDTO::getTraceCode,
                ReguIdentityDTO::getRegulatorId,
                (existingValue, newValue) -> newValue
            ));
    }

    private List<ReguIdentityDTO> fetchAllReguIdentityEntities(List<String> accountTraces) {
        List<ReguIdentityDTO> definitiveList = new ArrayList<>();
        List<List<String>> partitionedAccountTraces = ListUtils.partition(accountTraces, blockSize);

        partitionedAccountTraces.forEach(partition -> {
            List<ReguIdentityDTO> reguIdentityEntities = reguIdentityAdapterRepository.findByTraceCode(partition);
            definitiveList.addAll(reguIdentityEntities);
        });

        return definitiveList;
    }

    private void printTraceCodeRegulatorId(List<ReguIdentityDTO> definitiveList) {
        log.info("{}", new Gson().toJson(definitiveList));
    }

}
