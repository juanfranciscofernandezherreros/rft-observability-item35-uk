package com.sixgroup.refit.observability.item35.creator.domain.model.storage.response;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@lombok.Data
public class TimeSeries {

    private Metadata metadata;
    List<Data> data;
}
