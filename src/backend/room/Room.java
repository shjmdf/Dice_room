package backend.room;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/*
 * 房间实体类。
 */
public class Room {
    private final int id;
    private final String roomCode;
    private final int ownerId;
    private String name;
    private String description;
    private RoomStatus status;
    private final List<String> tags;
    private final List<RoomMember> members;
    private final LocalDateTime createdAt;

    public Room(int id, String roomCode, int ownerId, String name) {
        checkText(roomCode, "房间码");
        checkText(name, "房间名");

        this.id = id;
        this.roomCode = roomCode;
        this.ownerId = ownerId;
        this.name = name;
        this.description = "";
        this.status = RoomStatus.OPEN;
        this.tags = new ArrayList<>();
        this.members = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.members.add(new RoomMember(id, ownerId, RoomRole.OWNER));
    }

    public int getId() {
        return id;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public void rename(String name) {
        checkText(name, "房间名");
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public boolean isOpen() {
        return status.isOpen();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<String> getTags() {
        return new ArrayList<>(tags);
    }

    public void addTag(String tag) {
        checkText(tag, "标签");
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    public void removeTag(String tag) {
        tags.remove(tag);
    }

    public List<RoomMember> getMembers() {
        return new ArrayList<>(members);
    }

    public List<RoomMember> getActiveMembers() {
        List<RoomMember> result = new ArrayList<>();
        for (RoomMember member : members) {
            if (member.isActive()) {
                result.add(member);
            }
        }
        return result;
    }

    public RoomMember addMember(int userId, RoomRole role) {
        ensureOpen();

        if (findActiveMemberByUserId(userId) != null) {
            throw new IllegalStateException("用户已经在房间中");
        }

        RoomMember member = new RoomMember(id, userId, role);
        members.add(member);
        return member;
    }

    public void removeMember(int userId) {
        RoomMember member = requireActiveMember(userId);
        member.leave();
    }

    public RoomMember findActiveMemberByUserId(int userId) {
        for (RoomMember member : members) {
            if (member.getUserId() == userId && member.isActive()) {
                return member;
            }
        }
        return null;
    }

    public RoomMember requireActiveMember(int userId) {
        RoomMember member = findActiveMemberByUserId(userId);
        if (member == null) {
            throw new IllegalArgumentException("用户不在房间中");
        }
        return member;
    }

    public boolean hasActiveMember(int userId) {
        return findActiveMemberByUserId(userId) != null;
    }

    public void close() {
        status = RoomStatus.CLOSED;
    }

    public void delete() {
        status = RoomStatus.DELETED;
    }

    private void ensureOpen() {
        if (!status.isOpen()) {
            throw new IllegalStateException("房间不是开放状态");
        }
    }

    private void checkText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }
    }
}
