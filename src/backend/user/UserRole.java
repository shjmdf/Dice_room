package backend.user;

/**
 * 用户在系统层面的身份。
 *
 * USER 表示普通用户，可以创建房间、加入房间、聊天、掷骰和管理自己的角色卡。
 * ADMIN 表示系统管理员，除了上述功能，可以管理用户和邀请码。
 */
public enum UserRole {
    USER("User"),
    ADMIN("Administrator");

    private final String role;

    UserRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
