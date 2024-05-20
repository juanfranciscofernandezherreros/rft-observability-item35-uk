package com.sixgroup.refit.observability.item35.creator.infrastructure.mappper;

import com.sixgroup.refit.observability.item35.creator.configuration.RegulatorFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.RegulatorDTO;
import com.sixgroup.refit.observability.item35.creator.shared.utils.DateUtils;
import com.sixgroup.refit.observability.item35.creator.shared.utils.ReportUtils;
import com.sixgroup.refit.observability.modules.validate.domain.data.SlaInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegulatorMapper {

    public ReportGenerationDto toReportGenerationDto(final RegulatorDTO regulator,
                                                     final RegulatorFileTypeProperties fileTypeProperties,
                                                     final SlaInfo slaInfo) {
        final ReportGenerationDto reportGenerationDto = new ReportGenerationDto();
        reportGenerationDto.setReportName(getReportName(regulator.getFileName(), regulator.getFileType(), fileTypeProperties));
        reportGenerationDto.setReportType(getReportType(regulator.getFileName(), fileTypeProperties));
        reportGenerationDto.setReportGenerationTime(DateUtils.localDateTimeToIsoFormat(slaInfo.getDifferenceFromInit()));
        reportGenerationDto.setReportCompletionTime(DateUtils.localDateTimeToIsoFormat(regulator.getCreationDate()));
        //TODO: PUBLICATION TIME, PENDING NOW USE creationDate
        reportGenerationDto.setReportPublicationTime(DateUtils.localDateTimeToIsoFormat(regulator.getCreationDate()));
        reportGenerationDto.setDate(DateUtils.localDateTimeToSpainDateFormat(regulator.getReportingSession()));
        reportGenerationDto.setSla(DateUtils.localDateTimeToIsoFormat(slaInfo.getExpectSlaDate()));
        if (Boolean.FALSE.equals(slaInfo.getMeetsSla()) && null != slaInfo.getDifferenceDuration()) {
            reportGenerationDto.setDifference(slaInfo.getDifferenceInBigDecimal().toString());
        }
        return reportGenerationDto;
    }

    private String getReportName(final String fileName, final String fileType, final RegulatorFileTypeProperties fileTypeProperties) {
        final String fileNameValue = fileName.split("_")[2];
        return fileNameValue + "-" + ReportUtils.getReportName(fileTypeProperties.getReports(), fileType);
    }

    private String getReportType(final String fileName, final RegulatorFileTypeProperties fileTypeProperties) {
        return fileName.contains("ESMA") ? fileTypeProperties.getReportTypeEsma() : fileTypeProperties.getReportTypeNca();
    }
}
