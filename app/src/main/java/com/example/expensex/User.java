package com.example.expensex;

public class User {
    public String name, email;

    public User() {
        // Default constructor required for Firebase
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
