package backend.message;

import java.time.LocalDateTime;

public class Message {
    private final int id;
    private final int roomId;
    private final int senderId;
    private final int receiverId; // 0表示公共消息，其他表示私聊消息
    private final MessageType type;
    private final MessageVisibility visibility;
    private String content;
    private final LocalDateTime timestamp;
    public Message(int id, int roomId, int senderId, int receiverId, 
                    MessageType type, MessageVisibility visibility, 
                    String content) {
        this.id = id;
        this.roomId = roomId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.type = type;
        this.visibility = visibility;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }
    public int getId() {
        return id;
    }
    public int getRoomId() {
        return roomId;
    }
    public int getSenderId() {
        return senderId;
    }
    public int getReceiverId() {
        return receiverId;
    }
    public MessageType getType() {
        return type;
    }
    public MessageVisibility getVisibility() {
        return visibility;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        this.content = content;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

}
