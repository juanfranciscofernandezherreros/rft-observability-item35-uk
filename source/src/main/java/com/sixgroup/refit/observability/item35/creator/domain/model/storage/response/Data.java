package com.sixgroup.refit.observability.item35.creator.domain.model.storage.response;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@lombok.Data
public class Data {

    private String timestamp;

    private AggregateStatistics aggregateStatistics;
}
