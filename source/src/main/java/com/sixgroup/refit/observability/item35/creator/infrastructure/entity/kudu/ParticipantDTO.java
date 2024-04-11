package com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class ParticipantDTO {

    private String fileType;
    private Timestamp reportingSession;
    private Timestamp initDate;
    private Timestamp endDate;

    public ParticipantDTO() {
    }

    public ParticipantDTO(String fileType, Timestamp reportingSession, Timestamp initDate, Timestamp endDate) {
        this.fileType = fileType;
        this.reportingSession = reportingSession;
        this.initDate = initDate;
        this.endDate = endDate;
    }

}
