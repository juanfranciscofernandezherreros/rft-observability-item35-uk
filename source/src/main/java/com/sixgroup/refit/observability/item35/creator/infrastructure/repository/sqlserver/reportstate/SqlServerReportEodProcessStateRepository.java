package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.sqlserver.reportstate;

import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.sqlserver.ReportEoDDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.sqlserver.ReportEoDStateEntity;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.sqlserver.ReportEoDStatePK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SqlServerReportEodProcessStateRepository extends JpaRepository<ReportEoDStateEntity, ReportEoDStatePK> {
    @Query("SELECT new com.sixgroup.refit.observability.item35.creator.infrastructure.entity.sqlserver" +
        ".ReportEoDDTO(rEoD.reportType, rEoD.startedDate, rEoD.reportingSession) " +
        "FROM ReportEoDStateEntity as rEoD " +
        "WHERE rEoD.reportingSession >= :startTime AND rEoD.reportingSession < :endTime " +
        "AND rEoD.targetType = 'AUTHORITY' " +
        "AND rEoD.reportingProcess = 'EOD' " +
        "ORDER BY rEoD.startedDate"
    )
    List<ReportEoDDTO> findParticipantsByDayAccountAndFileType(@Param("startTime") String startTime,
                                                               @Param("endTime") String endTime);
}
