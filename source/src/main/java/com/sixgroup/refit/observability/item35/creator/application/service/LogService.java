package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class LogService {

    private LogService() {
    }

    public static void logInfo(final String message, final ItemCommandDTO itemCommandDTO) {
        log.info(message + ", with itemId: {}, itemType: {}, command {}",
            itemCommandDTO.getItemId(),
            itemCommandDTO.getItemType(),
            itemCommandDTO.getCommand());
    }
}
