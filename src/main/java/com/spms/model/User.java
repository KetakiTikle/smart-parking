package com.spms.model;

public class User {
    private final int userId;
    private final String name;
    private final String email;
    private final String phone;
    private final String passwordHash;
    private final String role;

    public User(int userId, String name, String email, String phone, String passwordHash, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
}
