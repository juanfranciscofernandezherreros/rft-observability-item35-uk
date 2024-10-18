package com.sixgroup.refit.observability.item35.creator.domain.repository.reportstate;

import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.sqlserver.ReportEoDDTO;

import java.util.List;

public interface ReportEodProcessStateRepository {
    List<ReportEoDDTO> find(String initDate, String endDate);
}
