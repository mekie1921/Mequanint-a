package application;

import java.sql.Timestamp;

public class Message {
    private int id;
    private int senderId;
    private int receiverId;
    private String senderName; // Helper for UI
    private String message;
    private Timestamp timestamp;

    public Message(int id, int senderId, int receiverId, String senderName, String message, Timestamp timestamp) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.senderName = senderName;
        this.message = message;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getMessage() {
        return message;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
