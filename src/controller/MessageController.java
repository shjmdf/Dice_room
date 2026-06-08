package controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import backend.message.Message;
import backend.user.User;
import repository.Page;
import service.AuthService;
import service.MessageService;

@RestController
@RequestMapping("/api/rooms/{roomId}/messages")
public class MessageController {
    private final MessageService messageService;
    private final AuthService authService;

    public MessageController(MessageService messageService, AuthService authService) {
        this.messageService = messageService;
        this.authService = authService;
    }

    @PostMapping
    public MessageResponse sendTextMessage(
            @PathVariable int roomId,
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestBody SendTextMessageRequest request) {
        User currentUser = authService.requireUser(authorization);
        return toResponse(messageService.sendTextMessage(roomId, currentUser.getId(), request.content()));
    }

    @PostMapping("/private")
    public MessageResponse sendPrivateMessage(
            @PathVariable int roomId,
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestBody SendPrivateMessageRequest request) {
        User currentUser = authService.requireUser(authorization);
        return toResponse(messageService.sendPrivateMessage(roomId, currentUser.getId(), request.receiverId(), request.content()));
    }

    @PostMapping("/dice")
    public MessageResponse sendDiceMessage(
            @PathVariable int roomId,
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestBody SendTextMessageRequest request) {
        User currentUser = authService.requireUser(authorization);
        return toResponse(messageService.sendDiceMessage(roomId, currentUser.getId(), request.content()));
    }

    @PostMapping("/dice/private")
    public MessageResponse sendPrivateDiceMessage(
            @PathVariable int roomId,
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestBody SendTextMessageRequest request) {
        User currentUser = authService.requireUser(authorization);
        return toResponse(messageService.sendPrivateDiceMessage(roomId, currentUser.getId(), request.content()));
    }

    @PostMapping("/skill-check")
    public MessageResponse sendSkillCheckMessage(
            @PathVariable int roomId,
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestBody SendTextMessageRequest request) {
        User currentUser = authService.requireUser(authorization);
        return toResponse(messageService.sendSkillCheckMessage(roomId, currentUser.getId(), request.content()));
    }

    @GetMapping("/public")
    public List<MessageResponse> getPublicMessages(
            @PathVariable int roomId,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        User currentUser = authService.requireUser(authorization);
        return messageService.getVisibleMessages(roomId, currentUser.getId()).stream()
                .filter(message -> message.getVisibility().isPublic())
                .map(this::toResponse)
                .toList();
    }

    @GetMapping
    public List<MessageResponse> getVisibleMessages(
            @PathVariable int roomId,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        User currentUser = authService.requireUser(authorization);
        return messageService.getVisibleMessages(roomId, currentUser.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/paged")
    public Page<MessageResponse> getVisibleMessagesPaged(
            @PathVariable int roomId,
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        User currentUser = authService.requireUser(authorization);
        Page<Message> result = messageService.getVisibleMessages(roomId, currentUser.getId(), page, size);
        List<MessageResponse> items = result.items().stream().map(this::toResponse).toList();
        return new Page<>(items, result.page(), result.size(), result.total());
    }

    @GetMapping("/private")
    public List<MessageResponse> getPrivateMessages(
            @PathVariable int roomId,
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestParam int userBId) {
        User currentUser = authService.requireUser(authorization);
        return messageService.getPrivateMessages(roomId, currentUser.getId(), userBId).stream()
                .map(this::toResponse)
                .toList();
    }

    @DeleteMapping("/{messageId}")
    public void deleteOwnMessage(
            @PathVariable int messageId,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        User currentUser = authService.requireUser(authorization);
        messageService.deleteOwnMessage(currentUser.getId(), messageId);
    }

    public record SendTextMessageRequest(String content) {
    }

    public record SendPrivateMessageRequest(int receiverId, String content) {
    }

    private MessageResponse toResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getRoomId(),
                message.getSenderId(),
                message.getReceiverId(),
                message.getType().name(),
                message.getVisibility().name(),
                message.getContent(),
                message.getTimestamp().toString());
    }

    public record MessageResponse(
            int id,
            int roomId,
            int senderId,
            int receiverId,
            String type,
            String visibility,
            String content,
            String timestamp) {
    }
}
