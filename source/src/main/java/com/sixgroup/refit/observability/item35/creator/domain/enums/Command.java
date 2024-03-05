package com.sixgroup.refit.observability.item35.creator.domain.enums;

public enum Command {

    REQUEST("request"),
    RESPONSE("response");

    private final String description;

    Command(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
