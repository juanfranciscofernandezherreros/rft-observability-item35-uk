package com.sixgroup.refit.observability.item35.creator.domain.service;

import com.sixgroup.refit.observability.item35.creator.domain.model.RecordStatus;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface WriteFileItem35Service {

    public File writeFile(List<RecordStatus> recordStatuses,String filePath,String itemDate) throws IOException;
}
