package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.configuration.TrProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.domain.repository.control.ReportingFileAdapterRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.TrDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.mappper.TrMapper;
import com.sixgroup.refit.observability.item35.creator.shared.sla.SlaInfoRepository;
import com.sixgroup.refit.observability.item35.creator.shared.utils.DateUtils;
import com.sixgroup.refit.observability.item35.creator.shared.utils.LazyIterators;
import com.sixgroup.refit.observability.modules.validate.domain.data.SlaInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.AppConstants.TR_ENTITY;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrService {

    private final ReportingFileAdapterRepository reportingFileAdapterRepository;
    private final TrProperties fileTypeProperties;
    private final SlaInfoRepository slaInfoRepository;
    private final TrMapper trMapper = new TrMapper();

    public Iterator<ReportGenerationDto> iterateTr(final String initDate, final String endDate, final String itemDate) {
        Iterator<TrDTO> records = reportingFileAdapterRepository.iterateTrByDayAccountAndFileType(initDate, endDate);
        return LazyIterators.filterMap(records, record -> {
            Optional<SlaInfo> slaInfo = slaInfoRepository.getSlaInfo(
                TR_ENTITY, record.getFileType(), record.getReportingSession(), record.getCreationDate());

            if (slaInfo.isEmpty()) {
                log.error("Error to find SlaInfo with entity {}, reportName {}, reportSession {}, reportDate {}. Configure properties",
                    TR_ENTITY, record.getFileType(), record.getReportingSession(), record.getCreationDate());
                return Optional.empty();
            }

            ReportGenerationDto result = trMapper.toReportGenerationDto(record, fileTypeProperties, slaInfo.get());
            result.setReportingDate(DateUtils.itemDateFormatted(itemDate));
            return Optional.of(result);
        });
    }

    public List<ReportGenerationDto> findTr(final String initDate, final String endDate, final String itemDate) {
        List<ReportGenerationDto> results = new ArrayList<>();
        iterateTr(initDate, endDate, itemDate).forEachRemaining(results::add);
        return results;
    }
}
