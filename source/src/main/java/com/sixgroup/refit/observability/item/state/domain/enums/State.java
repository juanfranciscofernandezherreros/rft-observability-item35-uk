package com.sixgroup.refit.observability.item.state.domain.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum State {
    EXTERNAL_REQUEST_RECEIVED("external_request_received"),
    RENAMING_DATA("renaming_data"),
    RENAMED_DATA("renamed_data"),
    SENT_REQUEST("sent_request"),
    INTERNAL_REQUEST_RECEIVED("internal_request_received"),
    SAVING_INFORMATION("saving_information"),
    SAVED_INFORMATION("saved_information"),
    SENT_RESPONSE("sent_response"),
    RESPONSE_RECEIVED("response_received"),
    ITEM_ACTION_INITIALIZED("item_action_initialized"),
    ITEM_ACTION_COMPLETED("item_action_completed"),
    FINISH("finish"),
    ERROR("error");

    private final String name;

    State(String name) {
        this.name = name;
    }

    public static State findByName(String name) {
        return Arrays.stream(values()).filter(state -> state.name.equals(name)).findFirst().orElse(null);
    }

    public static State nextStep(String name, Boolean alternative) {
        State current = findByName(name);
        if (current == null || current == ERROR || current == FINISH) {
            return current;
        }
        return values()[current.ordinal() + 1];
    }
}
