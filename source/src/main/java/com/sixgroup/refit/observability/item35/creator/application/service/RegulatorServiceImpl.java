package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.configuration.RegulatorFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.domain.repository.ReportingFileRepository;
import com.sixgroup.refit.observability.item35.creator.domain.service.RegulatorService;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.RegulatorDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.mappper.RegulatorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.sixgroup.refit.observability.item35.creator.shared.utils.Utils.getItemDateFormatted;

@RequiredArgsConstructor
@Service
@Slf4j
public class RegulatorServiceImpl implements RegulatorService {

    private final ReportingFileRepository reportingFileRepository;
    private final RegulatorFileTypeProperties fileTypeProperties;
    private final RegulatorMapper regulatorMapper;

    @Override
    public List<ReportGenerationDto> findRegulator(final String initDate, final String endDate, String itemDate) {
        List<RegulatorDTO> regulations = reportingFileRepository.findRegulatorByDayAccountAndFileType(initDate, endDate);
        List<ReportGenerationDto> regulatorReportGenerationData = new ArrayList<>();

        if (regulations.isEmpty()) {
            return regulatorReportGenerationData;
        }

        regulations.forEach(regulator -> {
            ReportGenerationDto reportGenerationDto = regulatorMapper.toReportGenerationDto(regulator, fileTypeProperties);
            reportGenerationDto.setReportingDate(getItemDateFormatted(itemDate));
            regulatorReportGenerationData.add(reportGenerationDto);
        });

        return regulatorReportGenerationData;
    }



}
