package com.sixgroup.refit.observability.item35.creator.shared.sla;

import com.sixgroup.refit.observability.modules.validate.application.SlaValidator;
import com.sixgroup.refit.observability.modules.validate.domain.data.SlaInfo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@AllArgsConstructor
@Repository
public class LibrarySlaInfoRepository implements SlaInfoRepository {
    private final SlaValidator slaValidator;

    @Override
    public Optional<SlaInfo> getSlaInfo(final String entity, final String reportName, final LocalDateTime reportSession, final LocalDateTime reportDate) {
        return slaValidator.getSlaInfo(entity, reportName, reportSession, reportDate);
    }

    @Override
    public Optional<SlaInfo> getSlaInfo(final String entity, final String reportName, final LocalDateTime reportSession, final LocalDateTime reportInitDate, final LocalDateTime reportEndDate) {
        return slaValidator.getSlaInfo(entity, reportName, reportSession, reportInitDate, reportEndDate);
    }
}
