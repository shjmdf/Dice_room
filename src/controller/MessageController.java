package controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import backend.message.Message;
import service.MessageService;

@RestController
@RequestMapping("/api/rooms/{roomId}/messages")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public Message sendTextMessage(@PathVariable int roomId, @RequestBody SendTextMessageRequest request) {
        return messageService.sendTextMessage(roomId, request.senderId(), request.content());
    }

    @PostMapping("/private")
    public Message sendPrivateMessage(@PathVariable int roomId, @RequestBody SendPrivateMessageRequest request) {
        return messageService.sendPrivateMessage(roomId, request.senderId(), request.receiverId(), request.content());
    }

    @PostMapping("/dice")
    public Message sendDiceMessage(@PathVariable int roomId, @RequestBody SendTextMessageRequest request) {
        return messageService.sendDiceMessage(roomId, request.senderId(), request.content());
    }

    @PostMapping("/skill-check")
    public Message sendSkillCheckMessage(@PathVariable int roomId, @RequestBody SendTextMessageRequest request) {
        return messageService.sendSkillCheckMessage(roomId, request.senderId(), request.content());
    }

    @GetMapping("/public")
    public List<Message> getPublicMessages(@PathVariable int roomId) {
        return messageService.getPublicRoomMessages(roomId);
    }

    @GetMapping
    public List<Message> getVisibleMessages(@PathVariable int roomId, @RequestParam int userId) {
        return messageService.getVisibleMessages(roomId, userId);
    }

    @GetMapping("/private")
    public List<Message> getPrivateMessages(
            @PathVariable int roomId,
            @RequestParam int userAId,
            @RequestParam int userBId) {
        return messageService.getPrivateMessages(roomId, userAId, userBId);
    }

    @DeleteMapping("/{messageId}")
    public void deleteOwnMessage(
            @PathVariable int messageId,
            @RequestParam int userId) {
        messageService.deleteOwnMessage(userId, messageId);
    }

    public record SendTextMessageRequest(int senderId, String content) {
    }

    public record SendPrivateMessageRequest(int senderId, int receiverId, String content) {
    }
}
