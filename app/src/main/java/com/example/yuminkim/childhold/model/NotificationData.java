package com.example.yuminkim.childhold.model;

import org.json.JSONObject;

public class NotificationData {
    public String json;

    public NotificationData(String json) {
        this.json = json;
    }
    public JSONObject toJsonObject() {
        try {
            return new JSONObject(this.json);
        } catch (Exception e) {
            return null;
        }
    }
}


