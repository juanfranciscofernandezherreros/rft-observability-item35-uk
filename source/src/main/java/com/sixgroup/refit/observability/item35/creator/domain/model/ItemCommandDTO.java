package com.sixgroup.refit.observability.item35.creator.domain.model;

import com.sixgroup.refit.observability.topic.item.ItemCommand;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;


@Builder
@Data
public class ItemCommandDTO {

    private String itemId;
    private String itemType;
    private String command;
    private String itemDate;
    private String fileUrl;
    private String fileName;

    public static ItemCommandDTO generateItemCommandDTO(ItemCommand itemCommand) {

        return ItemCommandDTO.builder()
            .itemId(itemCommand.getItemId())
            .itemType(itemCommand.getItemType())
            .command(itemCommand.getCommand())
            .itemDate(itemCommand.getItemDate())
            .fileUrl(Objects.nonNull(itemCommand.getFileInfo()) ? itemCommand.getFileInfo().getFileUrl() : StringUtils.EMPTY)
            .fileName(Objects.nonNull(itemCommand.getFileInfo()) ? itemCommand.getFileInfo().getFileName() : StringUtils.EMPTY)
            .build();
    }

}
