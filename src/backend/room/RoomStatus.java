package backend.room;
/*
 * 房间状态类。
 * 定义了房间的状态类型。
*/
public enum RoomStatus {
    OPEN("Open"),
    CLOSED("Closed"),
    DELETED("Deleted");

    private final String displayName;

    RoomStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isOpen() {
        return this == OPEN;
    }
}
