package com.sixgroup.refit.observability.item35.creator.domain.model;


import lombok.*;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ReguIdentityDTO {

    private String regulatorId;

    private String traceCode;
}
