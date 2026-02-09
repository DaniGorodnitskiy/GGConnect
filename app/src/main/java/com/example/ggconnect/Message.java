package com.example.ggconnect;

public class Message {
    private String senderId;   // מי שלח?
    private String receiverId; // למי שלחנו?
    private String content;    // תוכן ההודעה
    private long timestamp;    // מתי נשלחה (בשביל למיין)

    // בנאי ריק (חובה בשביל Firebase!)
    public Message() {}

    public Message(String senderId, String receiverId, String content) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = System.currentTimeMillis(); // הזמן הנוכחי
    }

    // Getters
    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }
}