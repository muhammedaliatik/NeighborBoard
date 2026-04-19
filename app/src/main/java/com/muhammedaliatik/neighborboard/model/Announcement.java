package com.muhammedaliatik.neighborboard.model;

public class Announcement {
    private String id;
    private String title;
    private String content;
    private String senderName;
    private String senderUid;
    private String apartmentCode;
    private long timestamp;

    public Announcement() {}

    public Announcement(String id, String title, String content, String senderName, String senderUid, String apartmentCode, long timestamp) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.senderName = senderName;
        this.senderUid = senderUid;
        this.apartmentCode = apartmentCode;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderUid() { return senderUid; }
    public void setSenderUid(String senderUid) { this.senderUid = senderUid; }

    public String getApartmentCode() { return apartmentCode; }
    public void setApartmentCode(String apartmentCode) { this.apartmentCode = apartmentCode; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}