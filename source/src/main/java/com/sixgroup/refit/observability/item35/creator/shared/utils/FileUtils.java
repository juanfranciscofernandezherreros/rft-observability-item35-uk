package com.sixgroup.refit.observability.item35.creator.shared.utils;

import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;

public final class FileUtils {
    private FileUtils() {
    }

    public static String getFileName(ItemCommandDTO itemCommandDTO) {
        return ItemType.getItemTypeFromName(itemCommandDTO.getItemType()).getNamePattern() + itemCommandDTO.getItemDate() + ".csv";
    }

}
