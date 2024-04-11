package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.configuration.TrFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.domain.repository.ReportingFileRepository;
import com.sixgroup.refit.observability.item35.creator.domain.service.TrService;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.TrDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.mappper.RegulatorMapper;
import com.sixgroup.refit.observability.item35.creator.infrastructure.mappper.TrMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.DATE_FORMAT_dd_MM_yyyy;
import static com.sixgroup.refit.observability.item35.creator.shared.utils.Utils.getItemDateFormatted;

@RequiredArgsConstructor
@Service
@Slf4j
public class TrServiceImpl implements TrService {

    private static final float TO_HOURS = 3600.00f;
    private final ReportingFileRepository reportingFileRepository;
    private final TrFileTypeProperties fileTypeProperties;
    private final TrMapper trMapper;

    @Override
    public List<ReportGenerationDto> findTr(final String initDate, final String endDate, String itemDate) {
        List<TrDTO> trs = reportingFileRepository.findTrByDayAccountAndFileType(initDate, endDate);
        List<ReportGenerationDto> trReportGenerationData = new ArrayList<>();

        if (trs.isEmpty()) {
            return trReportGenerationData;
        }

        trs.forEach(tr -> {
            ReportGenerationDto reportGenerationDto = trMapper.toReportGenerationDto(tr, fileTypeProperties);
            reportGenerationDto.setReportingDate(getItemDateFormatted(itemDate));
            trReportGenerationData.add(reportGenerationDto);
        });

        return trReportGenerationData;
    }

}
