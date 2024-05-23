package com.sixgroup.refit.observability.item35.creator.domain.service;

import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface WriteFileItem35Service<T> {

    File writeFile(List<T> records, ItemCommandDTO itemCommandDTO) throws IOException;
}
