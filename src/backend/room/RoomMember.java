package backend.room;

import java.time.LocalDateTime;
/*
 * 房间成员实体类。
 * 代表一个用户在一个房间中的状态。
*/
public class RoomMember {
    private final int roomId;
    private final int userId;
    private RoomRole role;
    private boolean muted;
    private Integer cardId;
    private String displayName;
    private final LocalDateTime joinedAt;
    private LocalDateTime leftAt;

    public RoomMember(int roomId, int userId, RoomRole role) {
        if (role == null) {
            throw new IllegalArgumentException("房间角色不能为空");
        }

        this.roomId = roomId;
        this.userId = userId;
        this.role = role;
        this.muted = false;
        this.cardId = null;
        this.displayName = "";
        this.joinedAt = LocalDateTime.now();
        this.leftAt = null;
    }

    public int getRoomId() {
        return roomId;
    }

    public int getUserId() {
        return userId;
    }

    public RoomRole getRole() {
        return role;
    }

    public void changeRole(RoomRole role) {
        if (role == null) {
            throw new IllegalArgumentException("房间角色不能为空");
        }
        this.role = role;
    }

    public boolean isMuted() {
        return muted;
    }

    public void mute() {
        muted = true;
    }

    public void unmute() {
        muted = false;
    }

    public Integer getCardId() {
        return cardId;
    }

    public void setCardId(Integer cardId) {
        this.cardId = cardId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void changeDisplayName(String displayName) {
        this.displayName = displayName == null ? "" : displayName.trim();
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public LocalDateTime getLeftAt() {
        return leftAt;
    }

    public boolean isActive() {
        return leftAt == null;
    }

    public void leave() {
        if (leftAt == null) {
            leftAt = LocalDateTime.now();
        }
    }
}
