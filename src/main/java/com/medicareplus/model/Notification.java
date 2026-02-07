package com.medicareplus.model;

public class Notification {

    private int notificationId;
    private String receiverType; // Patient / Doctor
    private int receiverId;
    private String message;
    private int isRead; // 0 or 1

    public Notification() {}

    // insert constructor
    public Notification(String receiverType, int receiverId, String message) {
        this.receiverType = receiverType;
        this.receiverId = receiverId;
        this.message = message;
        this.isRead = 0;
    }

    // read constructor
    public Notification(int notificationId, String receiverType, int receiverId, String message, int isRead) {
        this.notificationId = notificationId;
        this.receiverType = receiverType;
        this.receiverId = receiverId;
        this.message = message;
        this.isRead = isRead;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public String getReceiverType() {
        return receiverType;
    }

    public void setReceiverType(String receiverType) {
        this.receiverType = receiverType;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getIsRead() {
        return isRead;
    }

    public void setIsRead(int isRead) {
        this.isRead = isRead;
    }
}
