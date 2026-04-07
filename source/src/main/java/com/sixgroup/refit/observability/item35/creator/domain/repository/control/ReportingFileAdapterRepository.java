package com.sixgroup.refit.observability.item35.creator.domain.repository.control;

import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.ParticipantDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.RegulatorDTO;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.control.TrDTO;

import java.util.List;

public interface ReportingFileAdapterRepository {
    List<ParticipantDTO> findParticipantsByDayAccountAndFileType(String initDate, String endDate);

    List<ParticipantDTO> findParticipantsRecoFileType(String initDate, String endDate);

    List<RegulatorDTO> findRegulatorByDayAccountAndFileType(String initDate, String endDate);

    List<TrDTO> findTrByDayAccountAndFileType(String initDate, String endDate);

}

