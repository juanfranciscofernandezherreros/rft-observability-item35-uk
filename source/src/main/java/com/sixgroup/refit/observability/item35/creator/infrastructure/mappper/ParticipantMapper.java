package com.sixgroup.refit.observability.item35.creator.infrastructure.mappper;

import com.sixgroup.refit.observability.item35.creator.configuration.ParticipantFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.ParticipantDTO;
import com.sixgroup.refit.observability.item35.creator.shared.utils.DateUtils;
import com.sixgroup.refit.observability.item35.creator.shared.utils.ReportUtils;
import com.sixgroup.refit.observability.modules.validate.domain.data.SlaInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ParticipantMapper {

    public ReportGenerationDto toReportGenerationDto(final ParticipantDTO participant,
                                                     final ParticipantFileTypeProperties fileTypeProperties,
                                                     final SlaInfo slaInfo) {
        final ReportGenerationDto reportGenerationDto = new ReportGenerationDto();
        reportGenerationDto.setReportName(getReportName(participant.getFileType(), fileTypeProperties));
        reportGenerationDto.setReportType(fileTypeProperties.getReportType());
        reportGenerationDto.setReportGenerationTime(DateUtils.localDateTimeToIsoFormat(slaInfo.getDifferenceFromInit()));
        reportGenerationDto.setReportCompletionTime(DateUtils.localDateTimeToIsoFormat(participant.getEndDate()));
        //TODO: PUBLICATION TIME, PENDING NOW USE endDate
        reportGenerationDto.setReportPublicationTime(DateUtils.localDateTimeToIsoFormat(participant.getEndDate()));
        reportGenerationDto.setDate(DateUtils.localDateTimeToSpainDateFormat(participant.getReportingSession()));
        reportGenerationDto.setSla(DateUtils.localDateTimeToIsoFormat(slaInfo.getExpectSlaDate()));
        if (Boolean.FALSE.equals(slaInfo.getMeetsSla())) {
            reportGenerationDto.setDifference(DateUtils.localDateTimeToIsoFormat(slaInfo.getDifferenceFromInit()));
        } else {
            reportGenerationDto.setDifference("");
        }
        return reportGenerationDto;
    }

    private String getReportName(final String fileType, final ParticipantFileTypeProperties fileTypeProperties) {
        return ReportUtils.getReportName(fileTypeProperties.getReports(), fileType);
    }
}
