package com.example.barapp;

public class UserModel {
    private int id;
    private String username;
    private String password;
    private String role; // "ADMIN" or "MANAGER"
    private String fullName;

    public UserModel(int id, String username, String password, String role, String fullName) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isManager() {
        return "MANAGER".equals(role);
    }
}
