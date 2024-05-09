package com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private LocalDateTime reportingSessionTimeStamp;

    @Column(name = "creationtimestamp")
    private LocalDateTime creationTimestamp;

    @Column(name = "accountid")
    private String accountId;

}
