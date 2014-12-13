package com.ezzet.eulou.models;

public class MessageModel {

    private String id;
    private String fromUser;
    private String deletedByFrom;
    private String toUser;
    private String deletedByTo;
    private String message;
    private String messageTime;
    private String showId;

    public String getShowId() {
        return showId;
    }

    public void setShowId(String showId) {
        this.showId = showId;
    }

    public String getMessageId() {
        return id;
    }

    public void setMessageId(String id) {
        this.id = id;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getDeletedByFrom() {
        return deletedByFrom;
    }

    public void setDeletedByFrom(String deletedByFrom) {
        this.deletedByFrom = deletedByFrom;
    }

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public String getDeletedByTo() {
        return deletedByTo;
    }

    public void setDeletedByTo(String deletedByTo) {
        this.deletedByTo = deletedByTo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(String messageTime) {
        this.messageTime = messageTime;
    }

    public void setMesageTime(String mesageTime) {
        this.messageTime = mesageTime;
    }

}
