package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.kudu;


import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.RecordStatusDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.RecordStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

import static com.sixgroup.refit.observability.item35.creator.shared.Constants.*;

public interface RecordStatusKudu extends JpaRepository<RecordStatusEntity,Long> {

    @Query("SELECT new com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.RecordStatusDTO(rs.reportingDate , rs.messageType , rs.submissionChannel,COUNT(1)) " +
            "FROM RecordStatusEntity as rs  " +
            "WHERE rs.submissionChannel IN ('" + SFTP + "', '" + API + "', '" + WEB + "') " +
            "AND rs.reportingDate >= ?1 AND rs.reportingDate <= ?2 " +
            "GROUP BY rs.reportingDate, rs.messageType, rs.submissionChannel " +
            "ORDER BY rs.reportingDate")
   List<RecordStatusDTO> findByRecordStatus(String fromDate, String toDate);
}
