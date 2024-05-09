package com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ParticipantDTO {

    private String fileType;
    private LocalDateTime reportingSession;
    private LocalDateTime initDate;
    private LocalDateTime endDate;

    public ParticipantDTO() {
    }

    public ParticipantDTO(String fileType, LocalDateTime reportingSession, LocalDateTime initDate, LocalDateTime endDate) {
        this.fileType = fileType;
        this.reportingSession = reportingSession;
        this.initDate = initDate;
        this.endDate = endDate;
    }

}
