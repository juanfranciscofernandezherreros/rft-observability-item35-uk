package com.sixgroup.refit.observability.item35.creator.state.domain;

import com.sixgroup.refit.observability.item35.creator.shared.exception.BadRequestException;
import lombok.Getter;

import java.util.Optional;
import java.util.stream.Stream;

import static com.sixgroup.refit.observability.item35.creator.shared.ErrorCatalog.ERROR_CODE_BAD_REQUEST;

@Getter
public enum State {

    EXTERNAL_REQUEST_RECEIVED("external_request_received", "sent_request", "renaming_data"),
    RENAMING_DATA("renaming_data", "renamed_data", "-"),
    RENAMED_DATA("renamed_data", "sent_request", "-"),
    SENT_REQUEST("sent_request", "internal_request_received", "-"),
    INTERNAL_REQUEST_RECEIVED("internal_request_received", "saving_information", "-"),
    SAVING_INFORMATION("saving_information", "saved_information", "-"),
    SAVED_INFORMATION("saved_information", "sent_response", "-"),
    SENT_RESPONSE("sent_response", "response_received", "-"),
    RESPONSE_RECEIVED("response_received", "finish", "item_action_initialized"),
    ITEM_ACTION_INITIALIZED("item_action_initialized", "item_action_completed", "-"),
    ITEM_ACTION_COMPLETED("item_action_completed", "finish", "-"),
    FINISH("finish", "-", "-"),
    ERROR("error", "-", "-");

    private final String name;
    private final String next;
    private final String alternative;

    State(final String name, final String next, final String alternative) {
        this.name = name;
        this.next = next;
        this.alternative = alternative;
    }

    public static State findByName(final String name) {
        final Optional<State> found = Stream.of(State.values()).filter(value -> value.name.equals(name)).findFirst();
        if (found.isEmpty()) {
            throw new BadRequestException("Error to find state by name " + name, ERROR_CODE_BAD_REQUEST);
        }
        return found.get();
    }

    public static State nextStep(final String currentName, final Boolean decision) {
        final State currentState = findByName(currentName);
        return Boolean.TRUE.equals(decision) ? findByName(currentState.alternative) : findByName(currentState.next);
    }
}
