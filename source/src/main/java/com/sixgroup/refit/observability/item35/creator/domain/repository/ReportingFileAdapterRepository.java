package com.sixgroup.refit.observability.item35.creator.domain.repository;

import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.ParticipantDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.RegulatorDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.TrDTO;

import java.util.List;
import java.util.Optional;

public interface ReportingFileAdapterRepository {
    Optional<List<ParticipantDTO>> findParticipantsByDayAccountAndFileType(String initDate, String endDate);

    Optional<List<RegulatorDTO>> findRegulatorByDayAccountAndFileType(String initDate, String endDate);

    Optional<List<TrDTO>> findTrByDayAccountAndFileType(String initDate, String endDate);
}
