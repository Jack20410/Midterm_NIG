package com.tdtu.edu.vn.mygallery;

public class User {
    private String id;
    private String email;

    // Default constructor required for calls to DataSnapshot.getValue(User.class)
    public User() {}

    public User(String id, String email) {
        this.id = id;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
}
