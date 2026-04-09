package com.sixgroup.refit.observability.item35.creator.infrastructure.mappper;

import com.sixgroup.refit.observability.item35.creator.configuration.RegulatorProperties;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReguIdentityDTO;
import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.RegulatorDTO;
import com.sixgroup.refit.observability.item35.creator.shared.utils.DateUtils;
import com.sixgroup.refit.observability.item35.creator.shared.utils.ReportUtils;
import com.sixgroup.refit.observability.modules.validate.domain.data.SlaInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.AppConstants.*;

@Slf4j
public class RegulatorMapper {


    public ReportGenerationDto toReportGenerationDto(final RegulatorDTO regulator,
                                                     final RegulatorProperties fileTypeProperties,
                                                     final SlaInfo slaInfo,
                                                     final Map<String, ReguIdentityDTO> traceCodeRegulatorMap) {
        final ReportGenerationDto reportGenerationDto = new ReportGenerationDto();
        reportGenerationDto.setReportName(getReportName(regulator.getAccountId(), regulator.getFileType(), fileTypeProperties, regulator.getAccountTrace(), traceCodeRegulatorMap));
        reportGenerationDto.setReportType(getReportType(regulator.getFileName(), fileTypeProperties));
        reportGenerationDto.setReportGenerationTime(DateUtils.localDateTimeToIsoFormat(slaInfo.getDifferenceFromInit()));
        reportGenerationDto.setReportCompletionTime(DateUtils.localDateTimeToIsoFormat(regulator.getCreationDate()));
        reportGenerationDto.setReportPublicationTime(null);
        reportGenerationDto.setDate(DateUtils.localDateTimeToSpainDateFormat(regulator.getReportingSession()));
        reportGenerationDto.setSla(DateUtils.localDateTimeToIsoFormat(slaInfo.getExpectSlaDate()));
        if (Boolean.FALSE.equals(slaInfo.getMeetsSla()) && null != slaInfo.getDifferenceDuration()) {
            reportGenerationDto.setDifference(slaInfo.getDifferenceInBigDecimal().toString());
        }
        return reportGenerationDto;
    }

    private String getReportName(final String accountId,
                                 final String fileType,
                                 final RegulatorProperties fileTypeProperties,
                                 final String accountTrace,
                                 final Map<String, ReguIdentityDTO> traceCodeRegulatorMap) {

        String accountIdOutput = "";
        String origin = PORTAL_XML;
        if (Objects.isNull(traceCodeRegulatorMap.get(accountTrace))) {
            accountIdOutput = accountId;
        } else {
            final ReguIdentityDTO reguIdentityDTO = traceCodeRegulatorMap.get(accountTrace);
            if (EUDRITRACE.equals(accountId) || reguIdentityDTO.getTraceConnectivity()) {
                accountIdOutput = reguIdentityDTO.getTraceCode();
                origin = TRACE;
            } else if (reguIdentityDTO.isTranslatedAccount()) {
                accountIdOutput = reguIdentityDTO.getTraceCode();
                origin = PORTAL_XML;
            } else {
                accountIdOutput = accountId;
            }
        }

        return accountIdOutput + "-" + ReportUtils.getReportName(fileTypeProperties.getReports(), fileType) + origin;
    }

    private String getReportType(final String fileName,
                                 final RegulatorProperties fileTypeProperties) {
        return fileName.contains("ESMA") ? fileTypeProperties.getReportTypeEsma() : fileTypeProperties.getReportTypeFca();
    }
}
