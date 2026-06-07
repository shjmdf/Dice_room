package controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import backend.user.User;
import service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
    public UserResponse login(@RequestBody LoginRequest request) {
        User user = userService.loginUser(request.loginName(), request.password());
        return toResponse(user);
    }

    @GetMapping("/{userId}")
    public UserResponse getUser(@PathVariable int userId) {
        return toResponse(userService.requireUser(userId));
    }

    @GetMapping
    public List<UserResponse> getAllUsers(@RequestParam int adminId) {
        userService.requireAdmin(adminId);
        return userService.getAllUsers().stream()
                .map(this::toResponse)
                .toList();
    }

    @PatchMapping("/{userId}/password")
    public void changePassword(@PathVariable int userId, @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userId, request.password());
    }

    @PostMapping("/{targetUserId}/suspend")
    public void suspendUser(@PathVariable int targetUserId, @RequestBody AdminOperatorRequest request) {
        userService.suspendUserByAdmin(request.adminId(), targetUserId);
    }

    @PostMapping("/{targetUserId}/recover")
    public void recoverUser(@PathVariable int targetUserId, @RequestBody AdminOperatorRequest request) {
        userService.recoverUserByAdmin(request.adminId(), targetUserId);
    }

    @DeleteMapping("/{targetUserId}")
    public void deleteUser(@PathVariable int targetUserId, @RequestParam int adminId) {
        userService.deleteUserByAdmin(adminId, targetUserId);
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

    public record ChangePasswordRequest(String password) {
    }

    public record AdminOperatorRequest(int adminId) {
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
