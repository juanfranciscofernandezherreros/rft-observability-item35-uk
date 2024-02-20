package com.sixgroup.refit.observability.item35.creator.domain.enums;

public enum StatusFile {

    ITEM_REPORTING_OK("OK"),
    ITEM_REPORTING_ERROR("ERROR");

    private final String description;

    StatusFile(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
