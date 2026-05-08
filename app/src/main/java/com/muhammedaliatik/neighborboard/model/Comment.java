package com.muhammedaliatik.neighborboard.model;

public class Comment {
    private String id;
    private String text;
    private String userName;
    private String userUid;
    private long timestamp;

    public Comment() {}

    public Comment(String id, String text, String userName, String userUid, long timestamp) {
        this.id = id;
        this.text = text;
        this.userName = userName;
        this.userUid = userUid;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserUid() { return userUid; }
    public void setUserUid(String userUid) { this.userUid = userUid; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}