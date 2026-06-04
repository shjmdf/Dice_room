package backend.user;

/**
 * 邀请码的状态。
 *
 * ACTIVE 表示邀请码处于有效状态，可以被使用。
 * EXPIRED 表示邀请码已过期，无法再被使用。
 * USED 表示邀请码已被使用完毕，无法再次使用。
 * DISABLED 表示邀请码被管理员禁用。
 */
public enum InviteCodeStatus {
    ACTIVE("Active"),
    EXPIRED("Expired"),
    USED("Used"),
    DISABLED("Disabled");

    private String displayName;

    InviteCodeStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

}
