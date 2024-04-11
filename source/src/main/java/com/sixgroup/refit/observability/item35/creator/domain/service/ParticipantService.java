package com.sixgroup.refit.observability.item35.creator.domain.service;

import com.sixgroup.refit.observability.item35.creator.domain.model.ReportGenerationDto;

import java.util.List;

public interface ParticipantService {

     List<ReportGenerationDto> findParticipants(final String initDate, final String endDate, String itemDate);
}
