package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.kudu;

import com.sixgroup.refit.observability.item35.creator.configuration.ParticipantFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.RegulatorFileTypeProperties;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;


@Repository
@RequiredArgsConstructor
@Slf4j
public class ReportingFileRepository implements com.sixgroup.refit.observability.item35.creator.domain.repository.ReportingFileRepository {

    private final ReportingFileKudu reportingFileKudu;

    private final ParticipantFileTypeProperties participantFileTypeProperties;
    private final RegulatorFileTypeProperties regulatorFileTypeProperties;

    @Override
    public List<ParticipantDTO> findParticipantsByDayAccountAndFileType(String initDate, String endDate) {
        log.debug("Find Participants by day and fileType");
        Timestamp startDate = Timestamp.valueOf(LocalDate.parse(initDate).atStartOfDay());
        Timestamp finalDate = Timestamp.valueOf(LocalDate.parse(endDate).atStartOfDay());

       return reportingFileKudu.
            findParticipantsByDayAccountAndFileType(startDate, finalDate, participantFileTypeProperties.getREPORT_TYPE_QUERY());
    }

    @Override
    public List<RegulatorDTO> findRegulatorByDayAccountAndFileType(String initDate, String endDate) {
        log.debug("Find Regulators by day and fileType");
        Timestamp startDate = Timestamp.valueOf(LocalDate.parse(initDate).atStartOfDay());
        Timestamp endDateDate = Timestamp.valueOf(LocalDate.parse(endDate).atStartOfDay());
        return reportingFileKudu.
            findRegulatorByDayAccountAndFileType(startDate, endDateDate, regulatorFileTypeProperties.getREPORT_TYPE_QUERY());
    }

    @Override
    public List<TrDTO> findTrByDayAccountAndFileType(String initDate, String endDate) {
        log.debug("Find Regulators by day and fileType");
        Timestamp startDate = Timestamp.valueOf(LocalDate.parse(initDate).atStartOfDay());
        Timestamp endDateDate = Timestamp.valueOf(LocalDate.parse(endDate).atStartOfDay());
        return reportingFileKudu.findTrByDayAccountAndFileType(startDate, endDateDate);
    }

}
