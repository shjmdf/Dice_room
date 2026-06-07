package service;

import java.util.List;
import java.util.UUID;

import backend.room.Room;
import backend.room.RoomMember;
import backend.room.RoomRole;
import backend.room.RoomStatus;
import repository.RoomRepository;

public class RoomService {
    private final RoomRepository roomRepository;
    private final UserService userService;

    public RoomService(UserService userService, RoomRepository roomRepository) {
        if (userService == null) {
            throw new IllegalArgumentException("用户服务不能为空");
        }
        if (roomRepository == null) {
            throw new IllegalArgumentException("房间仓库不能为空");
        }
        this.userService = userService;
        this.roomRepository = roomRepository;
    }

    public Room createRoom(int ownerId, String name) {
        userService.requireUser(ownerId);
        return roomRepository.insert(generateUniqueRoomCode(), ownerId, name);
    }

    public Room joinRoom(int userId, String roomCode) {
        userService.requireUser(userId);

        Room room = requireRoomByCode(roomCode);
        if (!room.isOpen()) {
            throw new IllegalStateException("房间不是开放状态");
        }
        if (roomRepository.isActiveMember(room.getId(), userId)) {
            throw new IllegalStateException("用户已经在房间中");
        }

        roomRepository.insertMember(room.getId(), userId, RoomRole.PLAYER);
        return room;
    }

    public void leaveRoom(int userId, int roomId) {
        Room room = requireRoom(roomId);

        if (room.getOwnerId() == userId) {
            throw new IllegalStateException("房主不能直接离开房间，可以关闭或删除房间");
        }

        if (!roomRepository.isActiveMember(roomId, userId)) {
            throw new IllegalArgumentException("用户不在房间中");
        }

        roomRepository.leaveMember(roomId, userId);
    }

    public void closeRoom(int operatorId, int roomId) {
        requireOwnedRoom(operatorId, roomId);
        roomRepository.updateStatus(roomId, RoomStatus.CLOSED);
    }

    public void closeRoomByAdmin(int adminId, int roomId) {
        userService.requireAdmin(adminId);
        requireRoom(roomId);
        roomRepository.updateStatus(roomId, RoomStatus.CLOSED);
    }

    public void deleteRoom(int operatorId, int roomId) {
        requireOwnedRoom(operatorId, roomId);
        roomRepository.updateStatus(roomId, RoomStatus.DELETED);
    }

    public void deleteRoomByAdmin(int adminId, int roomId) {
        userService.requireAdmin(adminId);
        requireRoom(roomId);
        roomRepository.updateStatus(roomId, RoomStatus.DELETED);
    }

    public void changeMemberRole(int operatorId, int roomId, int targetUserId, RoomRole newRole) {
        if (newRole == null) {
            throw new IllegalArgumentException("房间角色不能为空");
        }

        Room room = requireOwnedRoom(operatorId, roomId);

        if (newRole == RoomRole.OWNER) {
            throw new IllegalArgumentException("暂不支持转让房主");
        }

        if (targetUserId == room.getOwnerId()) {
            throw new IllegalArgumentException("不能修改房主的房间角色");
        }

        requireActiveMember(roomId, targetUserId);
        roomRepository.updateMemberRole(roomId, targetUserId, newRole);
    }

    public void muteMember(int operatorId, int roomId, int targetUserId) {
        requireOwnedRoom(operatorId, roomId);
        requireActiveMember(roomId, targetUserId);
        roomRepository.updateMemberMuted(roomId, targetUserId, true);
    }

    public void unmuteMember(int operatorId, int roomId, int targetUserId) {
        requireOwnedRoom(operatorId, roomId);
        requireActiveMember(roomId, targetUserId);
        roomRepository.updateMemberMuted(roomId, targetUserId, false);
    }

    public void renameRoom(int operatorId, int roomId, String name) {
        Room room = requireOwnedRoom(operatorId, roomId);
        room.rename(name);
        roomRepository.updateBasicInfo(roomId, room.getName(), room.getDescription(), toTagsJson(room.getTags()));
    }

    public void setRoomDescription(int operatorId, int roomId, String description) {
        Room room = requireOwnedRoom(operatorId, roomId);
        room.setDescription(description);
        roomRepository.updateBasicInfo(roomId, room.getName(), room.getDescription(), toTagsJson(room.getTags()));
    }

    public void addRoomTag(int operatorId, int roomId, String tag) {
        Room room = requireOwnedRoom(operatorId, roomId);
        room.addTag(tag);
        roomRepository.updateBasicInfo(roomId, room.getName(), room.getDescription(), toTagsJson(room.getTags()));
    }

    public void removeRoomTag(int operatorId, int roomId, String tag) {
        Room room = requireOwnedRoom(operatorId, roomId);
        room.removeTag(tag);
        roomRepository.updateBasicInfo(roomId, room.getName(), room.getDescription(), toTagsJson(room.getTags()));
    }

    public Room findRoomById(int roomId) {
        return roomRepository.findById(roomId);
    }

    public Room findRoomByCode(String roomCode) {
        if (roomCode == null) {
            return null;
        }

        return roomRepository.findByRoomCode(roomCode);
    }

    public Room requireRoom(int roomId) {
        Room room = findRoomById(roomId);
        if (room == null) {
            throw new IllegalArgumentException("房间不存在");
        }
        return room;
    }

    public Room requireRoomByCode(String roomCode) {
        Room room = findRoomByCode(roomCode);
        if (room == null) {
            throw new IllegalArgumentException("房间不存在");
        }
        return room;
    }

    public Room requireJoinedRoom(int userId, int roomId) {
        Room room = requireRoom(roomId);
        if (!roomRepository.isActiveMember(roomId, userId)) {
            throw new IllegalStateException("用户不在房间中");
        }
        return room;
    }

    public List<Room> getRoomsByUserId(int userId) {
        return roomRepository.findByUserId(userId);
    }

    public List<RoomMember> getRoomMembers(int roomId) {
        requireRoom(roomId);
        return roomRepository.findActiveMembersByRoomId(roomId);
    }

    public void bindMemberCard(int userId, int roomId, Integer cardId) {
        requireJoinedRoom(userId, roomId);
        roomRepository.updateMemberCard(roomId, userId, cardId);
    }

    public void changeOwnRoomDisplayName(int userId, int roomId, String displayName) {
        requireJoinedRoom(userId, roomId);
        roomRepository.updateMemberDisplayName(roomId, userId, displayName == null ? "" : displayName.trim());
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public boolean isRoomMember(int roomId, int userId) {
        return findRoomById(roomId) != null && roomRepository.isActiveMember(roomId, userId);
    }

    private Room requireOwnedRoom(int operatorId, int roomId) {
        Room room = requireRoom(roomId);
        if (room.getOwnerId() != operatorId) {
            throw new IllegalStateException("只有房主可以操作房间");
        }
        return room;
    }

    private void requireActiveMember(int roomId, int userId) {
        if (!roomRepository.isActiveMember(roomId, userId)) {
            throw new IllegalArgumentException("用户不在房间中");
        }
    }

    private String generateUniqueRoomCode() {
        String code;
        do {
            code = UUID.randomUUID().toString()
                    .replace("-", "")
                    .substring(0, 8)
                    .toUpperCase();
        } while (findRoomByCode(code) != null);

        return code;
    }

    private String toTagsJson(List<String> tags) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < tags.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append("\"").append(tags.get(i).replace("\"", "\\\"")).append("\"");
        }
        builder.append("]");
        return builder.toString();
    }
}
