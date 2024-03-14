package com.sixgroup.refit.observability.item35.creator.domain.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Capacity {

    private String date;
    private String max;
    private String min;
    private String mean;
    private String typeCapacity;
}
