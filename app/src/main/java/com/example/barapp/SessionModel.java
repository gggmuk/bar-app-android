package com.example.barapp;

public class SessionModel {
    private int id;
    private int hallId;
    private int peopleCount;
    private long startTime;
    private long endTime;
    private String barItemsJson; // Using String to store JSON representation of items
    private double totalAmount;
    private String status;
    private boolean isSynced;

    public SessionModel(int id, int hallId, int peopleCount, long startTime, long endTime, String barItemsJson,
            double totalAmount, String status, boolean isSynced) {
        this.id = id;
        this.hallId = hallId;
        this.peopleCount = peopleCount;
        this.startTime = startTime;
        this.endTime = endTime;
        this.barItemsJson = barItemsJson;
        this.totalAmount = totalAmount;
        this.status = status;
        this.isSynced = isSynced;
    }

    public SessionModel(int hallId, int peopleCount, long startTime, String status) {
        this.hallId = hallId;
        this.peopleCount = peopleCount;
        this.startTime = startTime;
        this.status = status;
        this.barItemsJson = "[]";
        this.totalAmount = 0.0;
        this.isSynced = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHallId() {
        return hallId;
    }

    public void setHallId(int hallId) {
        this.hallId = hallId;
    }

    public int getPeopleCount() {
        return peopleCount;
    }

    public void setPeopleCount(int peopleCount) {
        this.peopleCount = peopleCount;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getBarItemsJson() {
        return barItemsJson;
    }

    public void setBarItemsJson(String barItemsJson) {
        this.barItemsJson = barItemsJson;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }
}
