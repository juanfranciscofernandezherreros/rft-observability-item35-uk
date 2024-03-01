package com.sixgroup.refit.observability.item35.creator.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ItemFileFinderRequest {
    private String itemType;
    private String fileName;
    private String fileUrl;

}
