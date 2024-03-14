package com.sixgroup.refit.observability.item35.creator.domain.model.storage.response;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class AggregateStatistics {
    private Float max;
    private Float min;
    private Float mean;
}
