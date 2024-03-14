package com.sixgroup.refit.observability.item35.creator.domain.service;


import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.topic.item.ItemCommand;
import com.sixgroup.refit.observability.topic.item.ItemId;

public interface ProducerItemService {

    void send(ItemCommandDTO itemCommandDTO);
}
