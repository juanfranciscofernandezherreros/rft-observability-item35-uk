package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.modules.log.rft.application.RftLog;
import com.sixgroup.refit.observability.modules.log.rft.domain.logobject.base.NameObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class LogService {

    public static void logInfo(String message, ItemCommandDTO itemCommandDTO) {
        RftLog.info(message, () ->
            List.of(NameObject.builder().name("timestamp").object(LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE)).build(),
                NameObject.builder().name("itemId").object(itemCommandDTO.getItemId()).build(),
                NameObject.builder().name("itemType").object(itemCommandDTO.getItemType()).build(),
                NameObject.builder().name("command").object(itemCommandDTO.getCommand()).build()));
    }
}
