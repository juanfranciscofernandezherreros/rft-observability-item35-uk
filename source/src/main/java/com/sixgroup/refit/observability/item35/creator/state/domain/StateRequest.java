package com.sixgroup.refit.observability.item35.creator.state.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StateRequest {
    private String itemType;
    private String fileName;
    private String fileUrl;
}
