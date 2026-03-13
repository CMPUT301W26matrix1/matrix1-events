package com.example.eventflow.model.entities;

import com.google.firebase.firestore.PropertyName;

public class Entrant {

    @PropertyName("entrant_id")
    private String entrantid;

    private String userId;
    private String name;
    private String status;

    public Entrant() {}

    @PropertyName("entrant_id")
    public String getEntrantid() {
        return entrantid;
    }

    @PropertyName("entrant_id")
    public void setEntrantid(String entrantid) {
        this.entrantid = entrantid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }
}