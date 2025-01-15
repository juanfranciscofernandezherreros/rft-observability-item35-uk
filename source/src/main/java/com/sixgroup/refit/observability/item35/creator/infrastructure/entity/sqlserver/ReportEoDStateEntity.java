package com.sixgroup.refit.observability.item35.creator.infrastructure.entity.sqlserver;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "report_eod_state")
@IdClass(ReportEoDStatePK.class)
public class ReportEoDStateEntity {

    @Id
    @Column(name = "report_type")
    private String reportType;

    @Id
    @Column(name = "reporting_session")
    private String reportingSession;

    @Id
    @Column(name = "target_type")
    private String targetType;

    @Id
    @Column(name = "reporting_process")
    private String reportingProcess;

    @Column(name = "started_date")
    @CreationTimestamp
    private LocalDateTime startedDate;

}
