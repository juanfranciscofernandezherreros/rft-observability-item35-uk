package com.sixgroup.refit.observability.item35.creator.shared.exception;

import lombok.Getter;

@Getter
public class BadRequestException extends RuntimeException {
    public final String message;
    public final String errorCode;

    public BadRequestException(final String message, final String errorCode) {
        this.message = message;
        this.errorCode = errorCode;
    }
}
