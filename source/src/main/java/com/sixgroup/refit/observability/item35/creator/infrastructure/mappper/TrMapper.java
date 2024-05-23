package com.sixgroup.refit.observability.item35.creator.infrastructure.mappper;

import com.sixgroup.refit.observability.item35.creator.configuration.TrFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.TrDTO;
import com.sixgroup.refit.observability.item35.creator.shared.utils.DateUtils;
import com.sixgroup.refit.observability.item35.creator.shared.utils.ReportUtils;
import com.sixgroup.refit.observability.modules.validate.domain.data.SlaInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TrMapper {
    public ReportGenerationDto toReportGenerationDto(final TrDTO trDTO,
                                                     final TrFileTypeProperties fileTypeProperties,
                                                     final SlaInfo slaInfo) {
        final ReportGenerationDto reportGenerationDto = new ReportGenerationDto();
        reportGenerationDto.setReportName(getReportName(trDTO.getAccountId(), trDTO.getFileType(), fileTypeProperties));
        reportGenerationDto.setReportType(fileTypeProperties.getReportType());
        reportGenerationDto.setReportGenerationTime(DateUtils.localDateTimeToIsoFormat(slaInfo.getDifferenceFromInit()));
        reportGenerationDto.setReportCompletionTime(DateUtils.localDateTimeToIsoFormat(trDTO.getCreationDate()));
        //TODO: PENDING PUBLICATION TIME
        reportGenerationDto.setReportPublicationTime(null);
        reportGenerationDto.setDate(DateUtils.localDateTimeToSpainDateFormat(trDTO.getReportingSession()));
        reportGenerationDto.setSla(DateUtils.localDateTimeToIsoFormat(slaInfo.getExpectSlaDate()));
        if (Boolean.FALSE.equals(slaInfo.getMeetsSla()) && null != slaInfo.getDifferenceDuration()) {
            reportGenerationDto.setDifference(slaInfo.getDifferenceInBigDecimal().toString());
        }
        return reportGenerationDto;
    }

    private String getReportName(final String accountId, final String fileType, final TrFileTypeProperties fileTypeProperties) {
        return accountId + "-" + ReportUtils.getReportName(fileTypeProperties.getReports(), fileType);
    }
}
