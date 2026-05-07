package com.muhammedaliatik.neighborboard.model;

public class SwapItem {
    private String id;
    private String name;
    private String description;
    private String ownerName;
    private String apartmentCode;
    private boolean available;
    private long timestamp;

    private String ownerUid;

    public SwapItem() {}

    public SwapItem(String id, String name, String description, String ownerName, String ownerUid, String apartmentCode, boolean available, long timestamp) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ownerName = ownerName;
        this.ownerUid = ownerUid;
        this.apartmentCode = apartmentCode;
        this.available = available;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getApartmentCode() { return apartmentCode; }
    public void setApartmentCode(String apartmentCode) { this.apartmentCode = apartmentCode; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getOwnerUid() { return ownerUid; }
    public void setOwnerUid(String ownerUid) { this.ownerUid = ownerUid; }
}