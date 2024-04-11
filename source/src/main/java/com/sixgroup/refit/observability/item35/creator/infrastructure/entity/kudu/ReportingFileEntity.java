package com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "reports_file_outgoing")
public class ReportingFileEntity {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "filetype")
    private String fileType;

    @Column(name = "outgoingfilename")
    private String outgoingFileName;

    @Column(name = "reportingsessiontimestamp")
    private Timestamp reportingSessionTimeStamp;

    @Column(name = "creationtimestamp")
    private Timestamp creationTimestamp;

    @Column(name = "accountid")
    private String accountId;

}
