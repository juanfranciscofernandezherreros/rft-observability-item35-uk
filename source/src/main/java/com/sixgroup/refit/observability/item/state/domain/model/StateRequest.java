package com.sixgroup.refit.observability.item.state.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StateRequest {
    private String itemType;
    private String fileName;
    private String fileUrl;
    private String errorDescription;
}
