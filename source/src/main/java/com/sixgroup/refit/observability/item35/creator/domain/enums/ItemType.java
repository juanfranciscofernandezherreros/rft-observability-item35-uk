package com.sixgroup.refit.observability.item35.creator.domain.enums;

public enum ItemType {
    SUBMISSION_VOLUMES("submissionVolumes");

    private final String description;

    ItemType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
