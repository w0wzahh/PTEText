package com.ptetext.model;

public class Contact {
    private int ownerId;
    private int contactId;
    private String nickname;
    private String username;

    public Contact() {}

    public Contact(int ownerId, int contactId, String nickname, String username) {
        this.ownerId = ownerId;
        this.contactId = contactId;
        this.nickname = nickname;
        this.username = username;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public int getContactId() {
        return contactId;
    }

    public void setContactId(int contactId) {
        this.contactId = contactId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}