package com.sixgroup.refit.observability.item35.creator.domain.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Data
public class StorageCapacityDto {

    private String reportingDate;
    private String date;
    private String timeStamp;
    private BigDecimal capacity;
    private BigDecimal usedCapacity;
    private BigDecimal availableCapacity;
    private BigDecimal utilization;

}
