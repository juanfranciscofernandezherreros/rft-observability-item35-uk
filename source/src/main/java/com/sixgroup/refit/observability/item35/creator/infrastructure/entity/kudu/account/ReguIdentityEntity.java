package com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.account;

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
@Table(name = "regu_identity")
public class ReguIdentityEntity {

    @Id
    @Column(name = "regulatorid")
    private String regulatorId;

    @Column(name = "tracecode")
    private String traceCode;
}
