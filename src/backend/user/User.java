package backend.user;

public class User {
    // 自定义信息
    private String avatarUrl;
    private String description;
    private String nickname;

    // 注册信息
    private String loginName;
    private String passwordHash;

    // 用户逻辑
    private UserStatus status;
    private UserRole role;
    private int id;

    // addons
    private String email;

    public User(int id, String loginName,
            String passwordHash, String nickname,
            UserRole role) {
        this.id = id;
        this.loginName = loginName;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.role = role;
        this.status = UserStatus.ACTIVE;
        this.avatarUrl = "";
        this.description = "";
        this.email = "";
    }

    public int getId() {
        return id;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl == null ? "" : avatarUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description;
    }

    public String getNickname() {
        return nickname;
    }

    public void changeNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("昵称不能为空");
        }
        this.nickname = nickname;
    }

    public String getLoginName() {
        return loginName;
    }

    public void changeLoginName(String loginName) {
        if (loginName == null || loginName.isBlank()) {
            throw new IllegalArgumentException("登录名不能为空");
        }
        this.loginName = loginName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void changePasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("密码哈希不能为空");
        }
        this.passwordHash = passwordHash;
    }

    public UserStatus getStatus() {
        return status;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isAdmin() {
        return role.isAdmin();
    }

    public boolean isActive() {
        return status.isActive();
    }

    public void suspend() {
        status = UserStatus.SUSPENDED;
    }

    public void recover() {
        status = UserStatus.ACTIVE;
    }

    public void delete() {
        status = UserStatus.DELETED;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? "" : email;
    }
}
