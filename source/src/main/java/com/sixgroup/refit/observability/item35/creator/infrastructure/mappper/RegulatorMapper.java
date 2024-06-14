package com.sixgroup.refit.observability.item35.creator.infrastructure.mappper;

import com.sixgroup.refit.observability.item35.creator.configuration.RegulatorFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.RegulatorDTO;
import com.sixgroup.refit.observability.item35.creator.shared.utils.DateUtils;
import com.sixgroup.refit.observability.item35.creator.shared.utils.ReportUtils;
import com.sixgroup.refit.observability.modules.validate.domain.data.SlaInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.*;

@Slf4j
public class RegulatorMapper {


    public ReportGenerationDto toReportGenerationDto(final RegulatorDTO regulator,
                                                     final RegulatorFileTypeProperties fileTypeProperties,
                                                     final SlaInfo slaInfo, Map<String, String> traceCodeRegulatorMap) {
        final ReportGenerationDto reportGenerationDto = new ReportGenerationDto();
        reportGenerationDto.setReportName(getReportName(regulator.getAccountId(), regulator.getFileType(), fileTypeProperties, regulator.getAccountTrace(), traceCodeRegulatorMap));
        reportGenerationDto.setReportType(getReportType(regulator.getFileName(), fileTypeProperties));
        reportGenerationDto.setReportGenerationTime(DateUtils.localDateTimeToIsoFormat(slaInfo.getDifferenceFromInit()));
        reportGenerationDto.setReportCompletionTime(DateUtils.localDateTimeToIsoFormat(regulator.getCreationDate()));
        //TODO: PENDING PUBLICATION TIME
        reportGenerationDto.setReportPublicationTime(null);
        reportGenerationDto.setDate(DateUtils.localDateTimeToSpainDateFormat(regulator.getReportingSession()));
        reportGenerationDto.setSla(DateUtils.localDateTimeToIsoFormat(slaInfo.getExpectSlaDate()));
        if (Boolean.FALSE.equals(slaInfo.getMeetsSla()) && null != slaInfo.getDifferenceDuration()) {
            reportGenerationDto.setDifference(slaInfo.getDifferenceInBigDecimal().toString());
        }
        return reportGenerationDto;
    }

    private String getReportName(final String accountId, final String fileType, final RegulatorFileTypeProperties fileTypeProperties, final String accountTrace, final Map<String, String> traceCodeRegulatorMap) {
        String result = accountId.equals(EUDRITRACE) && Objects.nonNull(traceCodeRegulatorMap.get(accountTrace)) ? traceCodeRegulatorMap.get(accountTrace) : accountId;
        return result + "-" + ReportUtils.getReportName(fileTypeProperties.getReports(), fileType) + (accountId.equals(EUDRITRACE) ? TRACE : PORTAL_XML);
    }

    private String getReportType(final String fileName, final RegulatorFileTypeProperties fileTypeProperties) {
        return fileName.contains("ESMA") ? fileTypeProperties.getReportTypeEsma() : fileTypeProperties.getReportTypeNca();
    }
}
