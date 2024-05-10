package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.configuration.TrFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.domain.repository.ReportingFileAdapterRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.TrDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.mappper.TrMapper;
import com.sixgroup.refit.observability.item35.creator.shared.sla.SlaInfoRepository;
import com.sixgroup.refit.observability.item35.creator.shared.utils.DateUtils;
import com.sixgroup.refit.observability.modules.validate.domain.data.SlaInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.TR_ENTITY;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrService {

    private final ReportingFileAdapterRepository reportingFileAdapterRepository;
    private final TrFileTypeProperties fileTypeProperties;
    private final SlaInfoRepository slaInfoRepository;
    private final TrMapper trMapper = new TrMapper();

    public List<ReportGenerationDto> findTr(final String initDate, final String endDate, final String itemDate) {
        final List<TrDTO> trs = reportingFileAdapterRepository.findTrByDayAccountAndFileType(initDate, endDate);
        if (trs.isEmpty()) {
            return new ArrayList<>();
        }

        final List<ReportGenerationDto> trReportGenerationData = new ArrayList<>();
        trs.forEach(tr -> {
            final Optional<SlaInfo> slaInfo = slaInfoRepository.getSlaInfo(TR_ENTITY, tr.getFileType(), tr.getReportingSession(), tr.getCreationDate());
            if (slaInfo.isEmpty()) {
                log.error("Error to find SlaInfo with entity {}, reportName {}, reportSession {}, reportDate {}. Configure properties",
                    TR_ENTITY, tr.getFileType(), tr.getReportingSession(), tr.getCreationDate());
            } else {
                final ReportGenerationDto reportGenerationDto = trMapper.toReportGenerationDto(tr, fileTypeProperties, slaInfo.get());
                reportGenerationDto.setReportingDate(DateUtils.itemDateFormatted(itemDate));
                trReportGenerationData.add(reportGenerationDto);
            }
        });

        return trReportGenerationData;
    }

}
