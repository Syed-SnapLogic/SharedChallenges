package com.workday.next.handler.issuer.model;

public class RevocationRequest extends PayloadModel {
    private String[] ids;

    public String[] getIds() {
        return ids;
    }

    public void setIds(String[] ids) {
        this.ids = ids;
    }
}
