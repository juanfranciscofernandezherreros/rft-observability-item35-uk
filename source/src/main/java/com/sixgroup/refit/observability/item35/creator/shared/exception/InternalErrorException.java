package com.sixgroup.refit.observability.item35.creator.shared.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InternalErrorException extends RuntimeException {
    private Exception exception;

    public InternalErrorException(final String message) {
        super(message);
    }

    public InternalErrorException(final String message, final Exception exception) {
        super(message);
        this.exception = exception;
    }
}
