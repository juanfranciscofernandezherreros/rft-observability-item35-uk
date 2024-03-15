package com.sixgroup.refit.observability.item35.creator.domain.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class Storage {

    private String timeStamp;
    private Float capacity;

    public Storage(String timeStamp, Float capacity) {
        this.timeStamp = timeStamp;
        this.capacity = capacity;
    }
}
