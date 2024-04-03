package com.sixgroup.refit.observability.item35.creator.domain.enums;

import java.util.stream.Stream;

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
        return Stream.of(Status.values()).filter(item -> description.equals(item.getDescription())).findFirst().get();
    }

}

