package com.example.eventflow.model.entities;

public class Entrant {

    private String entrantid;
    private String name;
    private String status;

    public Entrant() {
        // Required for Firestore
    }

    public String getEntrantid() {
        return entrantid;
    }

    public void setEntrantid(String entrantid) {
        this.entrantid = entrantid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}