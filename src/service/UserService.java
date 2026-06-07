package service;

/*
 * 用户服务类。
 * 注册用户
 * 登录用户
 * 按 id / loginName 查用户
 * 获取用户列表
 * 修改密码
 * 封禁用户
 * 恢复用户
 * 标记删除用户
 * requireUser：找不到就抛异常
 */
import java.util.List;

import backend.user.User;
import backend.user.UserRole;
import backend.user.UserStatus;
import backend.user.passwordHash.PasswordHasher;
import repository.UserRepository;

public class UserService {
    private final UserRepository userRepository;
    private final InviteCodeService inviteCodeService;
    private final PasswordHasher passwordHasher;

    public UserService(UserRepository userRepository, InviteCodeService inviteCodeService, PasswordHasher passwordHasher) {
        if (userRepository == null) {
            throw new IllegalArgumentException("用户仓库不能为空");
        }
        this.inviteCodeService = inviteCodeService;
        this.passwordHasher = passwordHasher;
        this.userRepository = userRepository;
    }

    public User registerUser(String loginName, String rawPassword, String nickname, String inviteCode) {
        checkText(loginName, "登录名");
        checkText(rawPassword, "密码");
        checkText(nickname, "昵称");

        if (findUserByLoginName(loginName) != null) {
            throw new IllegalArgumentException("登录名已存在");
        }

        if (!inviteCodeService.checkInviteCode(inviteCode)) {
            throw new IllegalArgumentException("邀请码无效");
        }

        String passwordHash = passwordHasher.hash(rawPassword);
        User newUser = userRepository.insert(loginName, passwordHash, nickname, UserRole.USER);
        inviteCodeService.useInviteCode(inviteCode);

        return newUser;
    }

    public User initializeAdmin(String loginName, String rawPassword, String nickname) {
        checkText(loginName, "管理员登录名");
        checkText(rawPassword, "管理员密码");
        checkText(nickname, "管理员昵称");

        if (hasAdmin()) {
            throw new IllegalStateException("管理员已经存在");
        }

        if (findUserByLoginName(loginName) != null) {
            throw new IllegalArgumentException("登录名已存在");
        }

        String passwordHash = passwordHasher.hash(rawPassword);
        return userRepository.insert(loginName, passwordHash, nickname, UserRole.ADMIN);
    }

    public User loginUser(String loginName, String rawPassword) {
        checkText(loginName, "登录名");
        checkText(rawPassword, "密码");

        User user = findUserByLoginName(loginName);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        if (!user.isActive()) {
            throw new IllegalStateException("用户状态不可登录");
        }

        if (!passwordHasher.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("密码错误");
        }

        return user;
    }

    public User findUserById(int id) {
        return userRepository.findById(id);
    }

    public User findUserByLoginName(String loginName) {
        if (loginName == null) {
            return null;
        }

        return userRepository.findByLoginName(loginName);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getAdmins() {
        return userRepository.findAdmins();
    }

    public boolean hasAdmin() {
        return userRepository.existsAdmin();
    }

    public void changePassword(int userId, String rawPassword) {
        checkText(rawPassword, "密码");
        User user = requireActiveUser(userId);
        String passwordHash = passwordHasher.hash(rawPassword);
        user.changePasswordHash(passwordHash);
        userRepository.updatePasswordHash(userId, passwordHash);
    }

    public void changeOwnPassword(int userId, String oldPassword, String newPassword) {
        checkText(oldPassword, "旧密码");
        checkText(newPassword, "新密码");

        User user = requireActiveUser(userId);
        if (!passwordHasher.matches(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("旧密码错误");
        }

        changePassword(userId, newPassword);
    }

    public User updateProfile(int userId, String nickname, String avatarUrl, String description, String email) {
        checkText(nickname, "昵称");

        User user = requireActiveUser(userId);
        user.changeNickname(nickname);
        user.setAvatarUrl(normalizeOptionalText(avatarUrl));
        user.setDescription(normalizeOptionalText(description));
        user.setEmail(normalizeOptionalText(email));

        userRepository.updateProfile(
                userId,
                user.getNickname(),
                user.getAvatarUrl(),
                user.getDescription(),
                user.getEmail());
        return requireActiveUser(userId);
    }

    public User changeLoginName(int userId, String loginName) {
        checkText(loginName, "登录名");

        User existing = findUserByLoginName(loginName);
        if (existing != null && existing.getId() != userId) {
            throw new IllegalArgumentException("登录名已存在");
        }

        User user = requireActiveUser(userId);
        user.changeLoginName(loginName);
        userRepository.updateLoginName(userId, loginName);
        return requireActiveUser(userId);
    }

    public void suspendUser(int userId) {
        User user = requireUser(userId);
        if (user.getStatus() == UserStatus.ACTIVE) {
            user.suspend();
            userRepository.updateStatus(userId, UserStatus.SUSPENDED);
        }
    }

    public void suspendUserByAdmin(int adminId, int targetUserId) {
        requireAdmin(adminId);
        if (adminId == targetUserId) {
            throw new IllegalArgumentException("管理员不能封禁自己");
        }
        suspendUser(targetUserId);
    }

    public void recoverUser(int userId) {
        User user = requireUser(userId);
        if (user.getStatus() == UserStatus.SUSPENDED) {
            user.recover();
            userRepository.updateStatus(userId, UserStatus.ACTIVE);
        }
    }

    public void recoverUserByAdmin(int adminId, int targetUserId) {
        requireAdmin(adminId);
        recoverUser(targetUserId);
    }

    public void deleteUser(int userId) {
        User user = requireUser(userId);
        user.delete();
        userRepository.softDelete(userId);
    }

    public void deleteUserByAdmin(int adminId, int targetUserId) {
        requireAdmin(adminId);
        if (adminId == targetUserId) {
            throw new IllegalArgumentException("管理员不能删除自己");
        }
        deleteUser(targetUserId);
    }

    public User requireUser(int userId) {
        User user = findUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }

    public User requireActiveUser(int userId) {
        User user = requireUser(userId);
        if (!user.isActive()) {
            throw new IllegalStateException("用户状态不可使用");
        }
        return user;
    }

    public User requireAdmin(int userId) {
        User user = requireActiveUser(userId);
        if (!user.isAdmin()) {
            throw new IllegalStateException("需要管理员权限");
        }
        return user;
    }

    private void checkText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }
    }

    private String normalizeOptionalText(String value) {
        return value == null ? "" : value.trim();
    }
}
