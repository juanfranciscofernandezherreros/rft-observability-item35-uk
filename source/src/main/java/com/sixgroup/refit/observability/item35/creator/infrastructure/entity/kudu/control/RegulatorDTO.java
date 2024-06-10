package com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RegulatorDTO {

    private String fileName;
    private String fileType;
    private LocalDateTime reportingSession;
    private String accountId;
    private LocalDateTime creationDate;
    private String accountTrace;
}
