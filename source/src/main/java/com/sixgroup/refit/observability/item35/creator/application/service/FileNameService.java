package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.configuration.ReportItemProperties;
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

    private final ReportItemProperties reportProperties;

    public String getFileName(final ItemType itemType, final String itemDate) {

        log.info("Generating filename for itemType: {} and itemDate: {}", itemType, itemDate);

        String fileNamePattern;

        switch (itemType) {
            case SUBMISSION_VOLUMES -> {
                fileNamePattern = reportProperties.getSubmissionVolumesFileNamePattern();
                log.info("Using SUBMISSION_VOLUMES pattern: {}", fileNamePattern);
            }
            case REPORT_GENERATION -> {
                fileNamePattern = reportProperties.getReportGenerationFileNamePattern();
                log.info("Using REPORT_GENERATION pattern: {}", fileNamePattern);
            }
            case STORAGE_CAPACITY -> {
                fileNamePattern = reportProperties.getStorageCapacityFileNamePattern();
                log.info("Using STORAGE_CAPACITY pattern: {}", fileNamePattern);
            }
            case COMPUTE_CAPACITY -> {
                fileNamePattern = reportProperties.getComputeCapacityFileNamePattern();
                log.info("Using COMPUTE_CAPACITY pattern: {}", fileNamePattern);
            }
            default -> {
                log.error("ItemType {} not supported for filename generation", itemType);
                throw new InternalErrorException("ItemType " + itemType + " not exists to fileName");
            }
        }

        log.info("Replacing '{}' in pattern '{}' with date '{}'", FILE_NAME_PATTERN_YYYYMMDD, fileNamePattern, itemDate);

        String finalFileName = fileNamePattern.replace(FILE_NAME_PATTERN_YYYYMMDD, itemDate);

        log.info("Final generated filename: {}", finalFileName);

        return finalFileName;
    }
}
