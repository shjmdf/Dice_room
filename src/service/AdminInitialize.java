package service;

import backend.user.User;

/*
 * 管理员启动逻辑。
 *
 * 当前版本先从参数或环境变量创建管理员。
 * 后续接数据库后，这里可以改成：从数据库读取管理员，或者数据库为空时初始化管理员。
 */

public class AdminInitialize {
    private static final String ADMIN_LOGIN_NAME_ENV = "DICE_ROOM_ADMIN_LOGIN_NAME";
    private static final String ADMIN_PASSWORD_ENV = "DICE_ROOM_ADMIN_PASSWORD";
    private static final String ADMIN_NICKNAME_ENV = "DICE_ROOM_ADMIN_NICKNAME";

    private AdminInitialize() {
    }

    public static User initialize(UserService userService, String loginName, String rawPassword, String nickname) {
        if (userService == null) {
            throw new IllegalArgumentException("用户服务不能为空");
        }

        return userService.initializeAdmin(loginName, rawPassword, nickname);
    }

    public static User initializeFromEnv(UserService userService) {
        String loginName = System.getenv(ADMIN_LOGIN_NAME_ENV);
        String rawPassword = System.getenv(ADMIN_PASSWORD_ENV);
        String nickname = System.getenv(ADMIN_NICKNAME_ENV);

        if (nickname == null || nickname.isBlank()) {
            nickname = "管理员";
        }

        return initialize(userService, loginName, rawPassword, nickname);
    }
}
