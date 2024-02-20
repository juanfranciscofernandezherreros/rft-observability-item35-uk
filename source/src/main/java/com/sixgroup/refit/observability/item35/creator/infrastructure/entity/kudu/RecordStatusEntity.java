package com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "emir_refit_int_control_refit", name = "record_status")
public class RecordStatusEntity {

    @Id
    @Column(name="id")
    private Long id;
    @Column(name="receiveddt")
    private String reportingDate;
    @Column(name = "status")
    private String messageType;
    @Column(name = "channel")
    private String submissionChannel;
    @Column(name="nomessagesOnGiveDate")
    private Integer nomessagesOnGiveDate;
}
