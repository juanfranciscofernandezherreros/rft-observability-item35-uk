package com.sixgroup.refit.observability.item35.creator.application.service;

import com.sixgroup.refit.observability.item35.creator.configuration.Regulation;
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
        validateRegulationSupport(itemType);
        String fileNamePattern = getPatternByItemType(itemType);
        log.info("Replacing '{}' in pattern '{}' with date '{}'", FILE_NAME_PATTERN_YYYYMMDD, fileNamePattern, itemDate);
        String finalFileName = fileNamePattern.replace(FILE_NAME_PATTERN_YYYYMMDD, itemDate);
        log.info("Final generated filename: {}", finalFileName);
        return finalFileName;
    }

    private void validateRegulationSupport(ItemType itemType) {
        if (Regulation.EU.equals(reportProperties.getRegulation()) && ItemType.STORAGE_CAPACITY.equals(itemType)) {
            log.error("ItemType STORAGE_CAPACITY is not supported for regulation: {}", Regulation.EU);
        }
    }

    private String getPatternByItemType(ItemType itemType) {
        return switch (itemType) {
            case SUBMISSION_VOLUMES -> reportProperties.getSubmissionVolumesFileNamePattern();
            case REPORT_GENERATION -> reportProperties.getReportGenerationFileNamePattern();
            case STORAGE_CAPACITY -> reportProperties.getStorageCapacityFileNamePattern();
            case COMPUTE_CAPACITY -> reportProperties.getComputeCapacityFileNamePattern();
            default -> {
                log.error("ItemType {} not supported for filename generation", itemType);
                throw new InternalErrorException("ItemType " + itemType + " does not exist for fileName");
            }
        };
    }
}
