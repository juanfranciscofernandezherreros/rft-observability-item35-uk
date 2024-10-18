package com.sixgroup.refit.observability.item35.creator.domain.model;


import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ReguIdentityDTO {
    @Expose
    private String traceCode;
    @Expose
    private String regulatorId;
    private Boolean traceConnectivity;
    private boolean isTranslatedAccount;
}
