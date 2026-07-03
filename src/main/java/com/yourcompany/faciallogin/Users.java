package com.yourcompany.faciallogin;


public class Users {
    private String name;
    private int userId; // unique user ID
    private int label;  // label used in face recognition

    public Users(String name, int userId, int label) {
        this.name = name;
        this.userId = userId;
        this.label = label;
    }

    public String getName() {
        return name;
    }
    public int getUserId() {
        return userId;
    }
    public int getLabel() {
        return label;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public void setLabel(int label) {
        this.label = label;
    }
}