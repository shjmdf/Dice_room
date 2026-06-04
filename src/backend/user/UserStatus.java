package backend.user;
/*
    * 用户在系统中的状态。
    *
    * ACTIVE 表示用户处于正常状态，可以使用系统的所有功能。
    * DELETED 表示用户被删除，所有数据被清除，无法恢复。
    * SUSPENDED 表示用户被暂时禁用，无法登录和使用系统的任何功能，但管理员可以恢复其状态。
*/

public enum UserStatus {
    ACTIVE("Active"),
    DELETED("Deleted"),
    SUSPENDED("Suspended");

    private String status;

    UserStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isDeleted() {
        return this == DELETED;
    }

    public boolean isSuspended() {
        return this == SUSPENDED;
    }
}
