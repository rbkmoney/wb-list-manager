package com.rbkmoney.wb.list.manager.exception;

public class RiakExecutionException extends RuntimeException {

    public RiakExecutionException() {
    }

    public RiakExecutionException(String message) {
        super(message);
    }

    public RiakExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public RiakExecutionException(Throwable cause) {
        super(cause);
    }

    public RiakExecutionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
