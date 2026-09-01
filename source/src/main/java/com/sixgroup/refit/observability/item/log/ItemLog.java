package com.sixgroup.refit.observability.item.log;

import com.sixgroup.refit.observability.item.state.domain.enums.State;
import com.sixgroup.refit.observability.item.state.domain.model.LogItemData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ItemLog {
    public void info(LogItemData data, State state) {
        log.info("[ITEM] type={} fileName={} fileUrl={} state={}", data.getItemType(), data.getFileName(),
            data.getFileUrl(), state.getName());
    }
}
