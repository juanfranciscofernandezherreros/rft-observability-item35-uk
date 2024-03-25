package com.sixgroup.refit.observability.item35.creator.domain.enums;

import java.util.stream.Stream;

import static com.sixgroup.refit.observability.item35.creator.shared.constants.CapacityConstants.*;
import static com.sixgroup.refit.observability.item35.creator.shared.constants.Constants.*;

public enum ItemType {
    SUBMISSION_VOLUMES("submissionVolumes", "TRRGS_EMIR_PR_IN_ND_ITEM35A_",
        new String[]{
            HEADER_TR_CODE,
            HEADER_DATE,
            HEADER_REGULATION_REFERENCE,
            HEADER_MESSAGE_TYPE,
            HEADER_SUBMISSION_CHANNEL,
            HEADER_NO_MESSAGES_ON_GIVE,
            HEADER_REPORTING_DATE
        }),

    COMPUTE_CAPACITY("computeCapacity", "TRRGS_EMIR_PR_IN_ND_ITEM35D_",
        new String[]{
            HEADER_TR_CODE,
            HEADER_DATE,
            HEADER_REGULATION_REFERENCE,
            HEADER_NAME,
            HEADER_DESCRIPTION,
            HEADER_CPU_RAM,
            HEADER_REPORTING_DATE,
            HEADER_MIN_USAGE,
            HEADER_AVG_USAGE,
            HEADER_MAX_USAGE,
            HEADER_INCIDENT_RELATED,
            HEADER_TR_INCIDENT_ID
        }),
    STORAGE_CAPACITY("storageCapacity", "TRRGS_EMIR_PR_IN_ND_ITEM35C_",
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
        });

    private final String name;
    private final String namePattern;

    private final String[] headers;

    ItemType(final String name, final String namePattern, String[] headers) {
        this.name = name;
        this.namePattern = namePattern;
        this.headers = headers;

    }

    public String getName() {
        return name;
    }

    public String getNamePattern() {
        return namePattern;
    }

    public String[] getHeaders() {
        return headers;
    }

    public static ItemType getItemTypeFromName(final String name) {
        return Stream.of(ItemType.values()).filter(item -> name.equals(item.getName())).findFirst().get();
    }

}
