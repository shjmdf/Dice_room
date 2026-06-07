package controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.room.Room;
import backend.room.RoomMember;
import backend.room.RoomRole;
import service.RoomService;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public Room createRoom(@RequestBody CreateRoomRequest request) {
        return roomService.createRoom(request.ownerId(), request.name());
    }

    @PostMapping("/join")
    public Room joinRoom(@RequestBody JoinRoomRequest request) {
        return roomService.joinRoom(request.userId(), request.roomCode());
    }

    @PostMapping("/{roomId}/leave")
    public void leaveRoom(@PathVariable int roomId, @RequestBody LeaveRoomRequest request) {
        roomService.leaveRoom(request.userId(), roomId);
    }

    @GetMapping("/{roomId}")
    public Room getRoom(@PathVariable int roomId) {
        return roomService.requireRoom(roomId);
    }

    @GetMapping("/user/{userId}")
    public List<Room> getRoomsByUserId(@PathVariable int userId) {
        return roomService.getRoomsByUserId(userId);
    }

    @GetMapping("/{roomId}/members")
    public List<RoomMember> getRoomMembers(@PathVariable int roomId) {
        return roomService.getRoomMembers(roomId);
    }

    @PatchMapping("/{roomId}/name")
    public void renameRoom(@PathVariable int roomId, @RequestBody RenameRoomRequest request) {
        roomService.renameRoom(request.operatorId(), roomId, request.name());
    }

    @PatchMapping("/{roomId}/description")
    public void setRoomDescription(@PathVariable int roomId, @RequestBody DescriptionRequest request) {
        roomService.setRoomDescription(request.operatorId(), roomId, request.description());
    }

    @PostMapping("/{roomId}/tags")
    public void addRoomTag(@PathVariable int roomId, @RequestBody TagRequest request) {
        roomService.addRoomTag(request.operatorId(), roomId, request.tag());
    }

    @DeleteMapping("/{roomId}/tags")
    public void removeRoomTag(@PathVariable int roomId, @RequestBody TagRequest request) {
        roomService.removeRoomTag(request.operatorId(), roomId, request.tag());
    }

    @PostMapping("/{roomId}/close")
    public void closeRoom(@PathVariable int roomId, @RequestBody OperatorRequest request) {
        roomService.closeRoom(request.operatorId(), roomId);
    }

    @DeleteMapping("/{roomId}")
    public void deleteRoom(@PathVariable int roomId, @RequestBody OperatorRequest request) {
        roomService.deleteRoom(request.operatorId(), roomId);
    }

    @PatchMapping("/{roomId}/members/{targetUserId}/role")
    public void changeMemberRole(
            @PathVariable int roomId,
            @PathVariable int targetUserId,
            @RequestBody ChangeRoleRequest request) {
        roomService.changeMemberRole(request.operatorId(), roomId, targetUserId, request.role());
    }

    @PostMapping("/{roomId}/members/{targetUserId}/mute")
    public void muteMember(
            @PathVariable int roomId,
            @PathVariable int targetUserId,
            @RequestBody OperatorRequest request) {
        roomService.muteMember(request.operatorId(), roomId, targetUserId);
    }

    @PostMapping("/{roomId}/members/{targetUserId}/unmute")
    public void unmuteMember(
            @PathVariable int roomId,
            @PathVariable int targetUserId,
            @RequestBody OperatorRequest request) {
        roomService.unmuteMember(request.operatorId(), roomId, targetUserId);
    }

    public record CreateRoomRequest(int ownerId, String name) {
    }

    public record JoinRoomRequest(int userId, String roomCode) {
    }

    public record LeaveRoomRequest(int userId) {
    }

    public record OperatorRequest(int operatorId) {
    }

    public record RenameRoomRequest(int operatorId, String name) {
    }

    public record DescriptionRequest(int operatorId, String description) {
    }

    public record TagRequest(int operatorId, String tag) {
    }

    public record ChangeRoleRequest(int operatorId, RoomRole role) {
    }
}
