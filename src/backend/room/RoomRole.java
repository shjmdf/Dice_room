package backend.room;
/*
 * 房间角色类。
 * 定义了用户在房间中的角色类型。
*/
public enum RoomRole {
    OWNER("Owner"),
    PLAYER("Player"),
    BOT("Bot");

    private final String displayName;

    RoomRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isOwner() {
        return this == OWNER;
    }
}
