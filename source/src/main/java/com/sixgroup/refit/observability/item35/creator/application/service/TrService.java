package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.configuration.TrProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.domain.repository.control.ReportingFileAdapterRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.TrDTO;
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
import java.util.concurrent.atomic.AtomicInteger;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.AppConstants.TR_ENTITY;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrService {

    private final ReportingFileAdapterRepository reportingFileAdapterRepository;
    private final TrProperties fileTypeProperties;
    private final SlaInfoRepository slaInfoRepository;
    private final TrMapper trMapper = new TrMapper();

    public List<ReportGenerationDto> findTr(final String initDate, final String endDate, final String itemDate) {
        log.info("[START] Entering findTr method. Input parameters: initDate='{}', endDate='{}', itemDate='{}'",
            initDate, endDate, itemDate);

        // 1. Fetching TR data
        log.info("[QUERY] Fetching TR records from Kudu via findTrByDayAccountAndFileType for range {} to {}", initDate, endDate);
        final List<TrDTO> trs = reportingFileAdapterRepository.findTrByDayAccountAndFileType(initDate, endDate);
        log.info("[QUERY RESULT] Found {} TR records.", trs.size());

        if (trs.isEmpty()) {
            log.info("[STOP] No TR records found for the given dates. Returning empty list.");
            return new ArrayList<>();
        }

        final List<ReportGenerationDto> trReportGenerationData = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(1);
        int totalToProcess = trs.size();

        // 2. Processing each TR record
        log.info("[PROCESS] Starting to process TR list...");
        trs.forEach(tr -> {
            int currentIdx = counter.getAndIncrement();
            log.info("[ITERATION {}/{}] Processing TR: FileType='{}', Session='{}', CreationDate='{}'",
                currentIdx, totalToProcess, tr.getFileType(), tr.getReportingSession(), tr.getCreationDate());

            // 3. SLA Lookup
            log.info("[SLA LOOKUP] Searching SlaInfo for Entity: '{}', FileType: '{}', Session: '{}'",
                TR_ENTITY, tr.getFileType(), tr.getReportingSession());

            final Optional<SlaInfo> slaInfo = slaInfoRepository.getSlaInfo(
                TR_ENTITY,
                tr.getFileType(),
                tr.getReportingSession(),
                tr.getCreationDate()
            );

            if (slaInfo.isEmpty()) {
                log.info("[SLA NOT FOUND] SlaInfo is missing for TR: {}, Session: {}", tr.getFileType(), tr.getReportingSession());
                log.error("Error to find SlaInfo with entity {}, reportName {}, reportSession {}, reportDate {}. Configure properties",
                    TR_ENTITY, tr.getFileType(), tr.getReportingSession(), tr.getCreationDate());
            } else {
                log.info("[SLA SUCCESS] SlaInfo retrieved successfully: {}", slaInfo.get());

                // 4. Mapping and Date Formatting
                log.info("[MAPPING] Mapping TR data and SLA info to ReportGenerationDto...");
                final ReportGenerationDto reportGenerationDto = trMapper.toReportGenerationDto(tr, fileTypeProperties, slaInfo.get());

                String formattedDate = DateUtils.itemDateFormatted(itemDate);
                reportGenerationDto.setReportingDate(formattedDate);

                log.info("[SUCCESS] DTO created for FileType: {}. Final ReportingDate: {}",
                    tr.getFileType(), reportGenerationDto.getReportingDate());

                trReportGenerationData.add(reportGenerationDto);
            }
        });

        log.info("[FINISH] findTr execution completed. Total DTOs generated: {}/{}",
            trReportGenerationData.size(), totalToProcess);

        return trReportGenerationData;
    }
}
