package com.example.barapp;

public class InventoryLogModel {
    private int id;
    private int itemId;
    private int quantityChange;
    private long timestamp;
    private String type; // "INCOME" or "EXPENSE"

    public InventoryLogModel(int id, int itemId, int quantityChange, long timestamp, String type) {
        this.id = id;
        this.itemId = itemId;
        this.quantityChange = quantityChange;
        this.timestamp = timestamp;
        this.type = type;
    }

    public InventoryLogModel(int itemId, int quantityChange, long timestamp, String type) {
        this.itemId = itemId;
        this.quantityChange = quantityChange;
        this.timestamp = timestamp;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getQuantityChange() {
        return quantityChange;
    }

    public void setQuantityChange(int quantityChange) {
        this.quantityChange = quantityChange;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
