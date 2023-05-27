package com.example.chateaseapp.model;

public class Status {
    private String imageUrl;
    private long timestamp;

    public Status(){}//Empty Constructor
    public Status(String imageUrl, long timestamp) { //Parameterised Constructor
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

