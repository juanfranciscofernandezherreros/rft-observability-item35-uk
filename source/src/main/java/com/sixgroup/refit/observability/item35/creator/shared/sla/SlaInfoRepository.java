package com.sixgroup.refit.observability.item35.creator.shared.sla;

import com.sixgroup.refit.observability.modules.validate.domain.data.SlaInfo;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SlaInfoRepository {
    Optional<SlaInfo> getSlaInfo(String entity, String reportName, LocalDateTime reportSession, LocalDateTime reportDate);
    Optional<SlaInfo> getSlaInfo(String entity, String reportName, LocalDateTime reportSession, LocalDateTime reportInitDate, LocalDateTime reportEndDate);
}
