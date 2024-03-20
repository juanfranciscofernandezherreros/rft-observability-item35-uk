package com.sixgroup.refit.observability.item35.creator.domain.service;


import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import org.apache.kafka.common.header.Headers;

public interface ProducerItemService {

    void send(ItemCommandDTO itemCommandDTO, Headers headers);
}
