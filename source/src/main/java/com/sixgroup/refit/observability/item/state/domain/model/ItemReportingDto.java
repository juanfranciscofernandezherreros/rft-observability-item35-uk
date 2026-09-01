package com.sixgroup.refit.observability.item.state.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ItemReportingDto implements LogItemData {
    private int id;
    private String itemType;
    private String fileName;
    private String fileUrl;
    private LocalDateTime fileCreationDate;
    private LocalDateTime fileUpdateDate;
    private String stateName;
    private LocalDateTime stateUpdateDate;
}
