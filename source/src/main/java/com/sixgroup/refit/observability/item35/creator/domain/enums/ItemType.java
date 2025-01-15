package com.sixgroup.refit.observability.item35.creator.domain.enums;

import com.sixgroup.refit.observability.item35.creator.shared.exception.InternalErrorException;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.AppConstants.*;
import static com.sixgroup.refit.observability.item35.creator.shared.constants.CapacityConstants.*;

@Slf4j
public enum ItemType {
    SUBMISSION_VOLUMES("submissionVolumes",
        new String[]{
            HEADER_TR_CODE,
            HEADER_REPORTING_DATE,
            HEADER_REGULATION_REFERENCE,
            HEADER_MESSAGE_TYPE,
            HEADER_SUBMISSION_CHANNEL,
            HEADER_NO_MESSAGES_ON_GIVE,
            HEADER_DATE
        }),
    REPORT_GENERATION("reportGeneration",
        new String[]{
            HEADER_TR_CODE,
            HEADER_REPORTING_DATE,
            HEADER_REGULATION_REFERENCE,
            HEADER_REPORT_NAME,
            HEADER_REPORT_TYPE,
            HEADER_REPORT_GENERATION_TIME,
            HEADER_REPORT_COMPLETION_TIME,
            HEADER_REPORT_PUBLICATION_TIME,
            HEADER_DATE,
            HEADER_SLA,
            HEADER_DIFFERENCE,
            HEADER_TR_INCIDENT_ID
        }),
    STORAGE_CAPACITY("storageCapacity",
        new String[]{
            HEADER_TR_CODE,
            HEADER_REPORTING_DATE,
            HEADER_REGULATION_REFERENCE,
            HEADER_DATA_CENTER_LOCATION,
            HEADER_DATABASE_SERVER_OR_PLATFORM,
            HEADER_DATE,
            HEADER_CAPACITY,
            HEADER_USED_CAPACITY,
            HEADER_AVAILABLE_CAPACITY,
            HEADER_UTILIZATION,
            HEADER_INCIDENT_RELATED,
            HEADER_TR_INCIDENT_ID
        }),
    COMPUTE_CAPACITY("computeCapacity",
        new String[]{
            HEADER_TR_CODE,
            HEADER_REPORTING_DATE,
            HEADER_REGULATION_REFERENCE,
            HEADER_NAME,
            HEADER_DESCRIPTION,
            HEADER_CPU_RAM,
            HEADER_DATE,
            HEADER_MIN_USAGE,
            HEADER_AVG_USAGE,
            HEADER_MAX_USAGE,
            HEADER_INCIDENT_RELATED,
            HEADER_TR_INCIDENT_ID
        });

    private final String name;
    private final String[] headers;

    ItemType(final String name, final String[] headers) {
        this.name = name;
        this.headers = headers;

    }

    public static ItemType getItemTypeFromName(final String name) {
        final Optional<ItemType> itemTypeFound = Arrays.stream(ItemType.values())
            .filter(itemType -> name.equals(itemType.getName())).findFirst();
        if (itemTypeFound.isEmpty()) {
            log.error("Error to find itemType by name {}", name);
            throw new InternalErrorException("Error to find itemType by name " + name);
        }
        return itemTypeFound.get();
    }

    public static List<String> reportsItemName() {
        return Arrays.stream(ItemType.values()).map(ItemType::getName).toList();
    }

    public String getName() {
        return name;
    }

    public String[] getHeaders() {
        return headers;
    }

}
