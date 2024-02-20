package com.sixgroup.refit.observability.item35.creator.infrastructure.entity.sqlserver;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Represents a series of stats, calculated from the information in Kudu table,
 * and then saved to observability SQL Server DB
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "item_reporting")
public class ItemReportingEntity {

    @Id
    @GeneratedValue
    private int id;

    @Column(name = "item_type")
    private String itemType;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "file_creation_date")
    @CreationTimestamp
    private LocalDate fileCreationDate;

    @Column(name = "file_update_date")
    @UpdateTimestamp
    private LocalDate fileUpdateDate;

    @Column(name = "state_name")
    private String stateName;

    @Column(name = "state_update_date")
    @UpdateTimestamp
    private LocalDate stateUpdateDate;

}
