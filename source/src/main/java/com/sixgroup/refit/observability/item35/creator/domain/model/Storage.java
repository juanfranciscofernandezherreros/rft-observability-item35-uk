package com.sixgroup.refit.observability.item35.creator.domain.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Data
public class Storage {

    private String timeStamp;
    private BigDecimal capacity;

    public Storage(String timeStamp, BigDecimal capacity) {
        this.timeStamp = timeStamp;
        this.capacity = capacity;
    }
}
