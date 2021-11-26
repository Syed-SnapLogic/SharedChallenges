package com.workday.next.utils.exception;

public class NotAuthorizedException extends RuntimeException {
    private int status;
    public NotAuthorizedException(final int status) {
        super();
        this.status = status;
    }

    public NotAuthorizedException(final String message) {
        super(message);
    }

    public NotAuthorizedException(final int status, final String message) {
        super(message);
        this.status = status;
    }
    public int getStatus() {
        return status;
    }

    public void setStatus(final int status) {
        this.status = status;
    }
}
