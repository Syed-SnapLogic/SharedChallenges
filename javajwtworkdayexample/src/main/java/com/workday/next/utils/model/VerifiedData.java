package com.workday.next.utils.model;

import java.util.ArrayList;
import java.util.Map;

public class VerifiedData {

    private String status;
    private String id;
    private String type;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<Map<String, String>> getAttributes() {
        return attributes;
    }

    public void setAttributes(ArrayList<Map<String, String>> attributes) {
        this.attributes = attributes;
    }

    private ArrayList<Map<String,String>> attributes;
}
