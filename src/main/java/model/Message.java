package model;

public class Message {
    public String from, to, text;
    public long timestamp;
    public boolean isSystem;
    // File message fields
    public String type, fileName, fileUrl;
    public long fileSize;

    public Message(String from, String to, String text, long timestamp) {
        this(from, to, text, timestamp, false, null, null, null, 0);
    }

    public Message(String from, String to, String text, long timestamp, boolean isSystem) {
        this(from, to, text, timestamp, isSystem, null, null, null, 0);
    }

    // Full constructor for file messages
    public Message(String from, String to, String text, long timestamp, boolean isSystem, String type, String fileName, String fileUrl, long fileSize) {
        this.from = from;
        this.to = to;
        this.text = text;
        this.timestamp = timestamp;
        this.isSystem = isSystem;
        this.type = type;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.fileSize = fileSize;
    }
} 