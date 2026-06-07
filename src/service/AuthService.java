package service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import backend.user.User;

public class AuthService {
    private static final long SESSION_TTL_SECONDS = 7L * 24L * 60L * 60L;

    private final UserService userService;
    private final SecureRandom secureRandom;
    private final Map<String, Session> sessions;

    public AuthService(UserService userService) {
        if (userService == null) {
            throw new IllegalArgumentException("用户服务不能为空");
        }
        this.userService = userService;
        this.secureRandom = new SecureRandom();
        this.sessions = new ConcurrentHashMap<>();
    }

    public LoginSession login(String loginName, String rawPassword) {
        User user = userService.loginUser(loginName, rawPassword);
        String token = generateToken();
        sessions.put(token, new Session(user.getId(), expiresAt()));
        return new LoginSession(token, user);
    }

    public void logout(String authorizationHeader) {
        sessions.remove(extractToken(authorizationHeader));
    }

    public User requireUser(String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        Session session = sessions.get(token);
        if (session == null) {
            throw new IllegalStateException("请先登录");
        }

        if (session.isExpired()) {
            sessions.remove(token);
            throw new IllegalStateException("登录已过期，请重新登录");
        }

        User user = userService.requireUser(session.userId());
        if (!user.isActive()) {
            sessions.remove(token);
            throw new IllegalStateException("用户状态不可使用");
        }

        sessions.put(token, new Session(user.getId(), expiresAt()));
        return user;
    }

    public User requireAdmin(String authorizationHeader) {
        User user = requireUser(authorizationHeader);
        if (!user.isAdmin()) {
            throw new IllegalStateException("需要管理员权限");
        }
        return user;
    }

    public void requireSelfOrAdmin(String authorizationHeader, int targetUserId) {
        User currentUser = requireUser(authorizationHeader);
        if (currentUser.getId() != targetUserId && !currentUser.isAdmin()) {
            throw new IllegalStateException("只能操作自己的数据");
        }
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new IllegalStateException("缺少登录凭证");
        }

        String token = authorizationHeader.trim();
        if (token.regionMatches(true, 0, "Bearer ", 0, "Bearer ".length())) {
            token = token.substring("Bearer ".length()).trim();
        }

        if (token.isBlank()) {
            throw new IllegalStateException("登录凭证不能为空");
        }
        return token;
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private long expiresAt() {
        return Instant.now().getEpochSecond() + SESSION_TTL_SECONDS;
    }

    private record Session(int userId, long expiresAt) {
        private boolean isExpired() {
            return Instant.now().getEpochSecond() > expiresAt;
        }
    }

    public record LoginSession(String token, User user) {
    }
}
