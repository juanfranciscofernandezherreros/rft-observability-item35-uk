package com.sixgroup.refit.observability.item35.creator.domain.enums;

import com.sixgroup.refit.observability.item35.creator.shared.exception.InternalErrorException;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Optional;

@Slf4j
public enum Status {

    ACCEPTED("ACPT"),
    REJECTED("RJCT");

    private final String description;

    Status(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static Status getStatusFromDescription(final String description) {
        final Optional<Status> statusFound = Arrays.stream(Status.values()).filter(status -> description.equals(status.getDescription())).findFirst();
        if (statusFound.isEmpty()) {
            log.error("Error to find status by description {}", description);
            throw new InternalErrorException("Error to find status by description " + description);
        }
        return statusFound.get();
    }

}

