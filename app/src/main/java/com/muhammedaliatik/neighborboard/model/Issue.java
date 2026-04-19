package com.muhammedaliatik.neighborboard.model;

public class Issue {
    private String id;
    private String title;
    private String description;
    private String location;
    private String status; // "açık" veya "kapalı"
    private String senderName;
    private String apartmentCode;
    private long timestamp;

    public Issue() {}

    public Issue(String id, String title, String description, String location, String status, String senderName, String apartmentCode, long timestamp) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.status = status;
        this.senderName = senderName;
        this.apartmentCode = apartmentCode;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getApartmentCode() { return apartmentCode; }
    public void setApartmentCode(String apartmentCode) { this.apartmentCode = apartmentCode; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}