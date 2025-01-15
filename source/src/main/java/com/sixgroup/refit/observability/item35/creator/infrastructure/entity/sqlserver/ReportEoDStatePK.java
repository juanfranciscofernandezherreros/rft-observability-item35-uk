package com.sixgroup.refit.observability.item35.creator.infrastructure.entity.sqlserver;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ReportEoDStatePK implements Serializable {

    @Column(name = "report_type")
    private String reportType;

    @Column(name = "reporting_session")
    private String reportingSession;

    @Column(name = "target_type")
    private String targetType;

    @Column(name = "reporting_process")
    private String reportingProcess;
}
