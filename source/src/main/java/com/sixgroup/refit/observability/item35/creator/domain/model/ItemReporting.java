package com.sixgroup.refit.observability.item35.creator.domain.model;

import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.enums.StatusFile;
import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.time.LocalDate;

@Data
@Builder
public class ItemReporting {

    private String itemType;
    private String fileName;
    private String fileUrl;
    private LocalDate fileCreationDate;
    private LocalDate fileUpdateDate;
    private String stateName;
    private LocalDate stateUpdateDate;

    public static ItemReporting builderItemReporting(File file){

       return ItemReporting
                .builder()
                .itemType(ItemType.SUBMISSION_VOLUMES.getDescription())
                .fileUrl(file.getAbsolutePath())
                .fileName(file.getName())
                .stateName(StatusFile.ITEM_REPORTING_OK.getDescription())
                .build();
    }

    public static ItemReporting builderItemReportingError(){
        return ItemReporting
                .builder()
                .itemType(ItemType.SUBMISSION_VOLUMES.getDescription())
                .fileUrl(StringUtils.EMPTY)
                .fileName(StringUtils.EMPTY)
                .stateName(StatusFile.ITEM_REPORTING_ERROR.getDescription())
                .build();
    }


}
