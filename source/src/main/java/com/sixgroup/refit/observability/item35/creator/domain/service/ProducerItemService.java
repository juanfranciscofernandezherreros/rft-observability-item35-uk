package com.sixgroup.refit.observability.item35.creator.domain.service;


import com.sixgroup.refit.observability.topic.item.ItemCommand;
import com.sixgroup.refit.observability.topic.item.ItemId;

public interface ProducerItemService {

    void send(ItemId itemId, ItemCommand itemCommand);
}
