package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.sqlserver.reportstate;

import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.sqlserver.ReportEoDDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.sqlserver.ReportEoDProcessStateEntity;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.sqlserver.ReportEoDProcessStatePK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SqlServerReportEodProcessStateRepository extends JpaRepository<ReportEoDProcessStateEntity, ReportEoDProcessStatePK> {
    @Query("SELECT new com.sixgroup.refit.observability.item35.creator.infrastructure.entity.sqlserver" +
        ".ReportEoDDTO(rEoD.reportType, rEoD.startedDate, rEoD.reportingSession) " +
        "FROM ReportEoDProcessStateEntity as rEoD " +
        "WHERE rEoD.reportingSession >= :startTime AND rEoD.reportingSession < :endTime " +
        "AND rEoD.targetType = 'AUTHORITY' " +
        "AND rEoD.queryId = 'empty' " +
        "ORDER BY rEoD.startedDate"
    )
    List<ReportEoDDTO> findParticipantsByDayAccountAndFileType(@Param("startTime") String startTime,
                                                               @Param("endTime") String endTime);
}
