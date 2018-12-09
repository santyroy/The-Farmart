package com.farmart.thefarmart.model;

public class User {

    private String name;
    private String email;
    private String imageURI;

    public User() {
    }

    public User(String name, String email, String imageURI) {
        this.name = name;
        this.email = email;
        this.imageURI = imageURI;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageURI() {
        return imageURI;
    }

    public void setImageURI(String imageURI) {
        this.imageURI = imageURI;
    }
}
