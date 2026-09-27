package com.example.barapp;

public class BarItemModel {
    private int id;
    private String name;
    private double price;
    private String category;
    private int currentStock;

    public BarItemModel(int id, String name, double price, String category, int currentStock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.currentStock = currentStock;
    }

    public BarItemModel(String name, double price, String category, int currentStock) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.currentStock = currentStock;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }
}
