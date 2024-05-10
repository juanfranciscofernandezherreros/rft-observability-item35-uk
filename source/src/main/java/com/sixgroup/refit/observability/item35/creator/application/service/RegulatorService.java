package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.configuration.RegulatorFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.domain.repository.ReportingFileAdapterRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.RegulatorDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.mappper.RegulatorMapper;
import com.sixgroup.refit.observability.item35.creator.shared.sla.SlaInfoRepository;
import com.sixgroup.refit.observability.item35.creator.shared.utils.DateUtils;
import com.sixgroup.refit.observability.modules.validate.domain.data.SlaInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.REGULATOR_ENTITY;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegulatorService {

    private final ReportingFileAdapterRepository reportingFileAdapterRepository;
    private final RegulatorFileTypeProperties fileTypeProperties;
    private final SlaInfoRepository slaInfoRepository;
    private final RegulatorMapper regulatorMapper = new RegulatorMapper();

    public List<ReportGenerationDto> findRegulator(final String initDate, final String endDate, final String itemDate) {
        final List<RegulatorDTO> regulations = reportingFileAdapterRepository.findRegulatorByDayAccountAndFileType(initDate, endDate);
        if (regulations.isEmpty()) {
            return new ArrayList<>();
        }

        final List<ReportGenerationDto> regulatorReportGenerationData = new ArrayList<>();
        regulations.forEach(regulator -> {
            final Optional<SlaInfo> slaInfo = slaInfoRepository.getSlaInfo(REGULATOR_ENTITY, regulator.getFileType(), regulator.getReportingSession(), regulator.getCreationDate());
            if (slaInfo.isEmpty()) {
                log.error("Error to find SlaInfo with entity {}, reportName {}, reportSession {}, reportDate {}. Configure properties",
                    REGULATOR_ENTITY, regulator.getFileType(), regulator.getReportingSession(), regulator.getCreationDate());
            } else {
                ReportGenerationDto reportGenerationDto = regulatorMapper.toReportGenerationDto(regulator, fileTypeProperties, slaInfo.get());
                reportGenerationDto.setReportingDate(DateUtils.itemDateFormatted(itemDate));
                regulatorReportGenerationData.add(reportGenerationDto);
            }
        });

        return regulatorReportGenerationData;
    }


}
