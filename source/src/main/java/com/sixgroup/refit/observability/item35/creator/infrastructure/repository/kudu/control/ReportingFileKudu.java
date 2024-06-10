package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.kudu.control;


import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.ParticipantDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.RegulatorDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.ReportingFileEntity;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.TrDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportingFileKudu extends JpaRepository<ReportingFileEntity, Long> {
    @Query("SELECT new com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control" +
        ".ParticipantDTO(rfo.fileType, MIN(rfo.reportingSessionTimeStamp), MIN(rfo.creationTimestamp), MAX(rfo.creationTimestamp)) " +
        "FROM ReportingFileEntity as rfo " +
        "WHERE rfo.reportingSessionTimeStamp >= :startTime AND rfo.reportingSessionTimeStamp < :endTime " +
        "AND rfo.accountId LIKE 'eudb%' " +
        "AND rfo.fileType IN (:fileTypes) " +
        "GROUP BY YEAR(rfo.reportingSessionTimeStamp), MONTH(rfo.reportingSessionTimeStamp), DAY(rfo.reportingSessionTimeStamp), rfo.fileType " +
        "ORDER BY MIN(rfo.reportingSessionTimeStamp)"
    )
    List<ParticipantDTO> findParticipantsByDayAccountAndFileType(@Param("startTime") LocalDateTime startTime,
                                                                 @Param("endTime") LocalDateTime endTime,
                                                                 @Param("fileTypes") List<String> fileTypes);

    @Query("SELECT new com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.RegulatorDTO(" +
        "rfo.outgoingFileName, " +
        "rfo.fileType, " +
        "rfo.reportingSessionTimeStamp, " +
        "rfo.accountId, " +
        "rfo.creationTimestamp, " +
        "SUBSTRING(rfo.outgoingFileName, " +
        "   LOCATE('_', rfo.outgoingFileName, LOCATE('_', rfo.outgoingFileName) + 1) + 1, " +
        "   LOCATE('_', rfo.outgoingFileName, LOCATE('_', rfo.outgoingFileName, LOCATE('_', rfo.outgoingFileName) + 1) + 1) - " +
        "   LOCATE('_', rfo.outgoingFileName, LOCATE('_', rfo.outgoingFileName) + 1) - 1" +
        ") AS accountTrace) " +
        "FROM ReportingFileEntity rfo " +
        "WHERE rfo.reportingSessionTimeStamp >= :startDate " +
        "AND rfo.reportingSessionTimeStamp < :endDate " +
        "AND rfo.accountId LIKE 'eudr%' " +
        "AND rfo.fileType IN :fileTypes " +
        "ORDER BY rfo.reportingSessionTimeStamp, rfo.accountId")
    List<RegulatorDTO> findRegulatorByDayAccountAndFileType(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("fileTypes") List<String> fileTypes);

    @Query("SELECT new com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control" +
        ".TrDTO(rfo.fileType, rfo.reportingSessionTimeStamp, rfo.accountId, rfo.creationTimestamp) " +
        "FROM ReportingFileEntity as rfo " +
        "WHERE (rfo.reportingSessionTimeStamp >= ?1 AND rfo.reportingSessionTimeStamp < ?2) " +
        "AND rfo.accountId LIKE 'tr%' " +
        "AND rfo.fileType IN (?3) " +
        "ORDER BY rfo.reportingSessionTimeStamp, rfo.accountId"
    )
    List<TrDTO> findTrByDayAccountAndFileType(LocalDateTime initDate, LocalDateTime endDate, List<String> fileTypes);
}
