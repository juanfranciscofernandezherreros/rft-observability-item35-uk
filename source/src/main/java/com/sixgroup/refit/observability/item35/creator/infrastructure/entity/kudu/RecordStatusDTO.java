package com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class RecordStatusDTO {
    private String reportingDate;
    private String messageType;
    private String submissionChannel;
    private long count;
}