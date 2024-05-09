package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public final class LogService {

    private LogService() {
    }

    public static void logInfo(final String message, final ItemCommandDTO itemCommandDTO) {
        log.info(message + ", with timestamp: {}, " + "itemId: {}, " + "itemType: {}, " + "command {}",
            LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE),
            itemCommandDTO.getItemId(),
            itemCommandDTO.getItemType(),
            itemCommandDTO.getCommand());
    }
}
