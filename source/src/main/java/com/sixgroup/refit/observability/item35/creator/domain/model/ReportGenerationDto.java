package com.sixgroup.refit.observability.item35.creator.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class ReportGenerationDto {

    private String reportingDate;
    private String reportName;
    private String reportType;
    private String reportGenerationTime;
    private String reportCompletionTime;
    private String reportPublicationTime;
    private String date;
    private String sla;
    private String difference;
}
