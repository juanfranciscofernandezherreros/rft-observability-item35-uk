package com.sixgroup.refit.observability.item.state.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "item_reporting")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemReportingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "item_reporting_generator")
    @SequenceGenerator(name = "item_reporting_generator", sequenceName = "item_reporting_seq", allocationSize = 1)
    private int id;
    @Column(name = "item_type")
    private String itemType;
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "file_url")
    private String fileUrl;
    @Column(name = "file_creation_date")
    private LocalDateTime fileCreationDate;
    @Column(name = "file_update_date")
    private LocalDateTime fileUpdateDate;
    @Column(name = "state_name")
    private String stateName;
    @Column(name = "state_update_date")
    private LocalDateTime stateUpdateDate;
}
