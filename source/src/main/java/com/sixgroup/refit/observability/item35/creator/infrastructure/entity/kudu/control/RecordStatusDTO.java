package com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
public class RecordStatusDTO {
    private LocalDateTime reportingDate;
    private String messageType;
    private String submissionChannel;
    private long count;
}
