package com.sixgroup.refit.observability.item35.creator.domain.repository;

import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.ParticipantDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.RegulatorDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.TrDTO;

import java.util.List;

public interface ReportingFileRepository {
    List<ParticipantDTO> findParticipantsByDayAccountAndFileType(final String initDate, final String endDate);

    List<RegulatorDTO> findRegulatorByDayAccountAndFileType(String initDate, String endDate);

    List<TrDTO> findTrByDayAccountAndFileType(String initDate, String endDate);
}
