package com.workday.next.handler.verifier.model;

public class PollingResponse {
    private boolean found;
    private boolean valid;

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
