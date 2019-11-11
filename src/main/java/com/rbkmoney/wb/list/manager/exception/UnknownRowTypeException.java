package com.rbkmoney.wb.list.manager.exception;

public class UnknownRowTypeException extends RuntimeException {

    public UnknownRowTypeException() {
    }

    public UnknownRowTypeException(String message) {
        super(message);
    }

    public UnknownRowTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownRowTypeException(Throwable cause) {
        super(cause);
    }

    public UnknownRowTypeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
