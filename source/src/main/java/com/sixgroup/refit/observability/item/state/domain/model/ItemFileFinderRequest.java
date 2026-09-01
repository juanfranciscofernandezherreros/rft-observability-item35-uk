package com.sixgroup.refit.observability.item.state.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemFileFinderRequest {
    private String itemType;
    private String fileName;
}
