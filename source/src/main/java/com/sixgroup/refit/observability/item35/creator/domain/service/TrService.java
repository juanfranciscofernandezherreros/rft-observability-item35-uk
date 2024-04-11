package com.sixgroup.refit.observability.item35.creator.domain.service;

import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;

import java.util.List;

public interface TrService {

     List<ReportGenerationDto> findTr(final String initDate, final String endDate, String itemDate);
}
