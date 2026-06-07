package service;

import java.util.List;

import backend.message.Message;
import backend.message.MessageType;
import backend.message.MessageVisibility;
import repository.MessageRepository;
import repository.Page;

public class MessageService {
    private final MessageRepository messageRepository;
    private final RoomService roomService;

    public MessageService(RoomService roomService, MessageRepository messageRepository) {
        if (roomService == null) {
            throw new IllegalArgumentException("房间服务不能为空");
        }
        if (messageRepository == null) {
            throw new IllegalArgumentException("消息仓库不能为空");
        }
        this.roomService = roomService;
        this.messageRepository = messageRepository;
    }

    public Message sendTextMessage(int roomId, int senderId, String content) {
        roomService.requireJoinedRoom(senderId, roomId);
        return createMessage(roomId, senderId, 0, MessageType.TEXT, MessageVisibility.PUBLIC, content);
    }

    public Message sendPrivateMessage(int roomId, int senderId, int receiverId, String content) {
        roomService.requireJoinedRoom(senderId, roomId);
        roomService.requireJoinedRoom(receiverId, roomId);

        return createMessage(roomId, senderId, receiverId, MessageType.TEXT, MessageVisibility.PRIVATE, content);
    }

    public Message sendDiceMessage(int roomId, int senderId, String content) {
        roomService.requireJoinedRoom(senderId, roomId);
        return createMessage(roomId, senderId, 0, MessageType.DICE, MessageVisibility.PUBLIC, content);
    }

    public Message sendSkillCheckMessage(int roomId, int senderId, String content) {
        roomService.requireJoinedRoom(senderId, roomId);
        return createMessage(roomId, senderId, 0, MessageType.SKILL_CHECK, MessageVisibility.PUBLIC, content);
    }

    public Message sendSystemMessage(int roomId, String content) {
        roomService.requireRoom(roomId);
        return createMessage(roomId, 0, 0, MessageType.SYSTEM, MessageVisibility.PUBLIC, content);
    }

    public List<Message> getPublicRoomMessages(int roomId) {
        roomService.requireRoom(roomId);
        return messageRepository.findByRoomId(roomId).stream()
                .filter(message -> message.getVisibility().isPublic())
                .toList();
    }

    public List<Message> getVisibleMessages(int roomId, int userId) {
        roomService.requireJoinedRoom(userId, roomId);
        return messageRepository.findVisibleMessages(roomId, userId);
    }

    public Page<Message> getVisibleMessages(int roomId, int userId, int page, int size) {
        roomService.requireJoinedRoom(userId, roomId);
        return messageRepository.findVisibleMessages(roomId, userId, page, size);
    }

    public List<Message> getPrivateMessages(int roomId, int userAId, int userBId) {
        roomService.requireJoinedRoom(userAId, roomId);
        roomService.requireJoinedRoom(userBId, roomId);
        return messageRepository.findPrivateMessages(roomId, userAId, userBId);
    }

    public Message findMessageById(int messageId) {
        return messageRepository.findById(messageId);
    }

    public Message requireMessage(int messageId) {
        Message message = findMessageById(messageId);
        if (message == null) {
            throw new IllegalArgumentException("消息不存在");
        }
        return message;
    }

    public void deleteOwnMessage(int userId, int messageId) {
        Message message = requireMessage(messageId);
        if (message.getSenderId() != userId) {
            throw new IllegalStateException("只能删除自己的消息");
        }
        messageRepository.deleteById(messageId);
    }

    private Message createMessage(int roomId, int senderId, int receiverId,
            MessageType type, MessageVisibility visibility, String content) {
        checkText(content, "消息内容");
        return messageRepository.insert(roomId, senderId, receiverId, type, visibility, content);
    }

    private void checkText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }
    }
}
