package com.sixgroup.refit.observability.item35.creator.domain.model.storage.response;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Data
public class Items {
    List<TimeSeries> timeSeries;
}
