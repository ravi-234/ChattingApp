package com.example.chateaseapp.model;

import java.util.ArrayList;

public class UserStatus {
    private String profileImage , name;
    private long lastUpdated;
    private ArrayList<Status> statuses;

    public UserStatus(){}           // empty constructor for firebase database

    public UserStatus(String profileImage, String name, long lastUpdated, ArrayList<Status> statuses) {
        this.profileImage = profileImage;
        this.name = name;
        this.lastUpdated = lastUpdated;
        this.statuses = statuses;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(ArrayList<Status> statuses) {
        this.statuses = statuses;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
