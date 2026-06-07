package controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import backend.room.Room;
import backend.room.RoomMember;
import backend.room.RoomRole;
import backend.user.User;
import service.AuthService;
import service.RoomService;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private final RoomService roomService;
    private final AuthService authService;

    public RoomController(RoomService roomService, AuthService authService) {
        this.roomService = roomService;
        this.authService = authService;
    }

    @PostMapping
    public RoomResponse createRoom(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestBody CreateRoomRequest request) {
        User currentUser = authService.requireUser(authorization);
        return toResponse(roomService.createRoom(currentUser.getId(), request.name()));
    }

    @PostMapping("/join")
    public RoomResponse joinRoom(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestBody JoinRoomRequest request) {
        User currentUser = authService.requireUser(authorization);
        return toResponse(roomService.joinRoom(currentUser.getId(), request.roomCode()));
    }

    @PostMapping("/{roomId}/leave")
    public void leaveRoom(
            @PathVariable int roomId,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        User currentUser = authService.requireUser(authorization);
        roomService.leaveRoom(currentUser.getId(), roomId);
    }

    @GetMapping("/{roomId}")
    public RoomResponse getRoom(
            @PathVariable int roomId,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        User currentUser = authService.requireUser(authorization);
        return toResponse(roomService.requireJoinedRoom(currentUser.getId(), roomId));
    }

    @GetMapping("/user/{userId}")
    public List<RoomResponse> getRoomsByUserId(
            @PathVariable int userId,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        authService.requireSelfOrAdmin(authorization, userId);
        return roomService.getRoomsByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/admin")
    public List<RoomResponse> getAllRooms(
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        authService.requireAdmin(authorization);
        return roomService.getAllRooms().stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{roomId}/members")
    public List<RoomMember> getRoomMembers(
            @PathVariable int roomId,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        User currentUser = authService.requireUser(authorization);
        roomService.requireJoinedRoom(currentUser.getId(), roomId);
        return roomService.getRoomMembers(roomId);
    }

    @PatchMapping("/{roomId}/name")
    public void renameRoom(
            @PathVariable int roomId,
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestBody RenameRoomRequest request) {
        User currentUser = authService.requireUser(authorization);
        roomService.renameRoom(currentUser.getId(), roomId, request.name());
    }

    @PatchMapping("/{roomId}/description")
    public void setRoomDescription(
            @PathVariable int roomId,
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestBody DescriptionRequest request) {
        User currentUser = authService.requireUser(authorization);
        roomService.setRoomDescription(currentUser.getId(), roomId, request.description());
    }

    @PostMapping("/{roomId}/tags")
    public void addRoomTag(
            @PathVariable int roomId,
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestBody TagRequest request) {
        User currentUser = authService.requireUser(authorization);
        roomService.addRoomTag(currentUser.getId(), roomId, request.tag());
    }

    @DeleteMapping("/{roomId}/tags")
    public void removeRoomTag(
            @PathVariable int roomId,
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestBody TagRequest request) {
        User currentUser = authService.requireUser(authorization);
        roomService.removeRoomTag(currentUser.getId(), roomId, request.tag());
    }

    @PostMapping("/{roomId}/close")
    public void closeRoom(
            @PathVariable int roomId,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        User currentUser = authService.requireUser(authorization);
        roomService.closeRoom(currentUser.getId(), roomId);
    }

    @DeleteMapping("/{roomId}")
    public void deleteRoom(
            @PathVariable int roomId,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        User currentUser = authService.requireUser(authorization);
        roomService.deleteRoom(currentUser.getId(), roomId);
    }

    @PostMapping("/admin/{roomId}/close")
    public void closeRoomByAdmin(
            @PathVariable int roomId,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        User admin = authService.requireAdmin(authorization);
        roomService.closeRoomByAdmin(admin.getId(), roomId);
    }

    @DeleteMapping("/admin/{roomId}")
    public void deleteRoomByAdmin(
            @PathVariable int roomId,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        User admin = authService.requireAdmin(authorization);
        roomService.deleteRoomByAdmin(admin.getId(), roomId);
    }

    @PatchMapping("/{roomId}/members/{targetUserId}/role")
    public void changeMemberRole(
            @PathVariable int roomId,
            @PathVariable int targetUserId,
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestBody ChangeRoleRequest request) {
        User currentUser = authService.requireUser(authorization);
        roomService.changeMemberRole(currentUser.getId(), roomId, targetUserId, request.role());
    }

    @PostMapping("/{roomId}/members/{targetUserId}/mute")
    public void muteMember(
            @PathVariable int roomId,
            @PathVariable int targetUserId,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        User currentUser = authService.requireUser(authorization);
        roomService.muteMember(currentUser.getId(), roomId, targetUserId);
    }

    @PostMapping("/{roomId}/members/{targetUserId}/unmute")
    public void unmuteMember(
            @PathVariable int roomId,
            @PathVariable int targetUserId,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        User currentUser = authService.requireUser(authorization);
        roomService.unmuteMember(currentUser.getId(), roomId, targetUserId);
    }

    @PutMapping("/{roomId}/members/card")
    public void bindMemberCard(
            @PathVariable int roomId,
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestBody BindCardRequest request) {
        User currentUser = authService.requireUser(authorization);
        roomService.bindMemberCard(currentUser.getId(), roomId, request.cardId());
    }

    @PatchMapping("/{roomId}/members/me/nickname")
    public void changeOwnRoomNickname(
            @PathVariable int roomId,
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestBody RoomNicknameRequest request) {
        User currentUser = authService.requireUser(authorization);
        roomService.changeOwnRoomDisplayName(currentUser.getId(), roomId, request.nickname());
    }

    public record CreateRoomRequest(String name) {
    }

    public record JoinRoomRequest(String roomCode) {
    }

    public record RenameRoomRequest(String name) {
    }

    public record DescriptionRequest(String description) {
    }

    public record TagRequest(String tag) {
    }

    public record ChangeRoleRequest(RoomRole role) {
    }

    public record BindCardRequest(Integer cardId) {
    }

    public record RoomNicknameRequest(String nickname) {
    }

    private RoomResponse toResponse(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getRoomCode(),
                room.getOwnerId(),
                room.getName(),
                room.getDescription(),
                room.getStatus().name(),
                room.getTags());
    }

    public record RoomResponse(
            int id,
            String roomCode,
            int ownerId,
            String name,
            String description,
            String status,
            List<String> tags) {
    }
}
