package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.modules.log.rft.application.RftLog;
import com.sixgroup.refit.observability.modules.log.rft.domain.logobject.base.NameObject;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class LogService {
    public static void logInfo(String message, ItemCommandDTO itemCommandDTO) {
        log.debug(message + ", with timestamp: {}, " + "itemId: {}, " + "itemType: {}, " + "command {}",
            LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE),
            itemCommandDTO.getItemId(),
            itemCommandDTO.getItemType(),
            itemCommandDTO.getCommand());
    }
}
