package com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu;

import lombok.Data;

import java.sql.Timestamp;


@Data
public class RegulatorDTO {

    public RegulatorDTO(String fileName, String fileType, Timestamp reportingSession, String accountId,
                        Timestamp creationDate) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.reportingSession = reportingSession;
        this.accountId = accountId;
        this.creationDate = creationDate;
    }

    public RegulatorDTO() {
    }

    private String fileName;
    private String fileType;
    private Timestamp reportingSession;
    private String accountId;
    private Timestamp creationDate;
}
