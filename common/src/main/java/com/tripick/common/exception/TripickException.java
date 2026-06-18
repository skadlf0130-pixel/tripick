package com.tripick.common.exception;

import lombok.Getter;

@Getter
public class TripickException extends RuntimeException {

    private final ErrorCode errorCode;

    public TripickException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}