package com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class TrDTO {

    private String fileType;
    private LocalDateTime reportingSession;
    private String accountId;
    private LocalDateTime creationDate;
}
