package com.sixgroup.refit.observability.item35.creator.infrastructure.entity.sqlserver;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReportEoDDTO {
    private String reportType;
    private LocalDateTime startedDate;
    private String reportingSession;

    public ReportEoDDTO(final String reportType, final LocalDateTime startedDate, final String reportingSession) {
        this.reportType = reportType;
        this.startedDate = startedDate;
        this.reportingSession = reportingSession;
    }
}
