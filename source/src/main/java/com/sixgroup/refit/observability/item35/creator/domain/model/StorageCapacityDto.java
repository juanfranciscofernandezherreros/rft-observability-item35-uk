package com.sixgroup.refit.observability.item35.creator.domain.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class StorageCapacityDto {

    private String reportingDate;
    private String date;
    private String timeStamp;
    private Float capacity;
    private Float usedCapacity;
    private Float availableCapacity;
    private Float utilization;

}
