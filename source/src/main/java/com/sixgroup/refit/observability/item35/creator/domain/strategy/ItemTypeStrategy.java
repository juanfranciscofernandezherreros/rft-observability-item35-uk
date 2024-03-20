package com.sixgroup.refit.observability.item35.creator.domain.strategy;

import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import org.apache.kafka.common.header.Headers;

import java.io.File;
import java.util.concurrent.ExecutionException;

public interface ItemTypeStrategy {

    File execute(ItemCommandDTO itemCommandDTO, Headers headers) throws ExecutionException, InterruptedException;

    ItemType getItemType();
}
