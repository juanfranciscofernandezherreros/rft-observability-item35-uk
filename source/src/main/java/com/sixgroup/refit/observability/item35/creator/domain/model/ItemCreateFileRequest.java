package com.sixgroup.refit.observability.item35.creator.domain.model;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ItemCreateFileRequest {
    private String itemType;
    private String fileName;
    private String fileUrl;
    private LocalDate fileCreationDate;
    private LocalDate fileUpdateDate;
    private String stateName;
    private LocalDate stateUpdateDate;
}
