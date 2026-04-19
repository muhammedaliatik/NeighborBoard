package com.muhammedaliatik.neighborboard.model;

public class User {
    private String uid;
    private String email;
    private String displayName;
    private String apartmentCode;

    public User() {} // Firebase için boş constructor zorunlu

    public User(String uid, String email, String displayName, String apartmentCode) {
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.apartmentCode = apartmentCode;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getApartmentCode() { return apartmentCode; }
    public void setApartmentCode(String apartmentCode) { this.apartmentCode = apartmentCode; }
}