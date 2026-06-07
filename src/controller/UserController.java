package controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import backend.user.User;
import service.AuthService;
import service.AuthService.LoginSession;
import service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public UserResponse register(@RequestBody RegisterRequest request) {
        User user = userService.registerUser(
                request.loginName(),
                request.password(),
                request.nickname(),
                request.inviteCode());
        return toResponse(user);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        LoginSession session = authService.login(request.loginName(), request.password());
        return new LoginResponse(session.token(), toResponse(session.user()));
    }

    @PostMapping("/admin/initialize")
    public UserResponse initializeAdmin(@RequestBody InitializeAdminRequest request) {
        User admin = userService.initializeAdmin(request.loginName(), request.password(), request.nickname());
        return toResponse(admin);
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader(name = "Authorization", required = false) String authorization) {
        authService.logout(authorization);
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(@RequestHeader(name = "Authorization", required = false) String authorization) {
        return toResponse(authService.requireUser(authorization));
    }

    @GetMapping("/{userId}")
    public UserResponse getUser(
            @PathVariable int userId,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        authService.requireSelfOrAdmin(authorization, userId);
        return toResponse(userService.requireUser(userId));
    }

    @GetMapping
    public List<UserResponse> getAllUsers(
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        authService.requireAdmin(authorization);
        return userService.getAllUsers().stream()
                .map(this::toResponse)
                .toList();
    }

    @PatchMapping("/{userId}/password")
    public void changePassword(
            @PathVariable int userId,
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestBody ChangePasswordRequest request) {
        authService.requireSelfOrAdmin(authorization, userId);
        userService.changePassword(userId, request.password());
    }

    @PostMapping("/{targetUserId}/suspend")
    public void suspendUser(
            @PathVariable int targetUserId,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        User admin = authService.requireAdmin(authorization);
        userService.suspendUserByAdmin(admin.getId(), targetUserId);
    }

    @PostMapping("/{targetUserId}/recover")
    public void recoverUser(
            @PathVariable int targetUserId,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        User admin = authService.requireAdmin(authorization);
        userService.recoverUserByAdmin(admin.getId(), targetUserId);
    }

    @DeleteMapping("/{targetUserId}")
    public void deleteUser(
            @PathVariable int targetUserId,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        User admin = authService.requireAdmin(authorization);
        userService.deleteUserByAdmin(admin.getId(), targetUserId);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getLoginName(),
                user.getNickname(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getAvatarUrl(),
                user.getDescription(),
                user.getEmail());
    }

    public record RegisterRequest(String loginName, String password, String nickname, String inviteCode) {
    }

    public record LoginRequest(String loginName, String password) {
    }

    public record InitializeAdminRequest(String loginName, String password, String nickname) {
    }

    public record LoginResponse(String token, UserResponse user) {
    }

    public record ChangePasswordRequest(String password) {
    }

    public record UserResponse(
            int id,
            String loginName,
            String nickname,
            String role,
            String status,
            String avatarUrl,
            String description,
            String email) {
    }
}
