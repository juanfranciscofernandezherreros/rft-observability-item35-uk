package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.configuration.ReportProperties;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.shared.exception.InternalErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.AppConstants.FILE_NAME_PATTERN_YYYYMMDD;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileNameService {

    private final ReportProperties reportProperties;

    public String getFileName(final ItemType itemType, final String itemDate) {
        String fileNamePatters;
        switch (itemType) {
            case SUBMISSION_VOLUMES -> fileNamePatters = reportProperties.submissionVolumesFileNamePattern;
            case REPORT_GENERATION -> fileNamePatters = reportProperties.reportGenerationFileNamePattern;
            case STORAGE_CAPACITY -> fileNamePatters = reportProperties.storageCapacityFileNamePattern;
            case COMPUTE_CAPACITY -> fileNamePatters = reportProperties.computeCapacityFileNamePattern;
            default -> throw new InternalErrorException("ItemType " + itemType + "not exists to fileName");
        }
        return fileNamePatters.replace(FILE_NAME_PATTERN_YYYYMMDD, itemDate);
    }

}
