package controller;

import java.util.Date;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import backend.user.InviteCode;
import service.InviteCodeService;
import service.UserService;

@RestController
@RequestMapping("/api/invite-codes")
public class InviteCodeController {
    private final InviteCodeService inviteCodeService;
    private final UserService userService;

    public InviteCodeController(InviteCodeService inviteCodeService, UserService userService) {
        this.inviteCodeService = inviteCodeService;
        this.userService = userService;
    }

    @PostMapping
    public InviteCodeResponse generateInviteCode(@RequestBody GenerateInviteCodeRequest request) {
        userService.requireAdmin(request.adminId());
        InviteCode inviteCode = inviteCodeService.generateInviteCode(
                request.usageLimit(),
                new Date(request.expirationTimestampMillis()),
                request.adminId());
        return toResponse(inviteCode);
    }

    @GetMapping
    public List<InviteCodeResponse> getInviteCodes(@RequestParam int adminId) {
        userService.requireAdmin(adminId);
        return inviteCodeService.getInviteCodes().stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/{code}/disable")
    public void disableInviteCode(@PathVariable String code, @RequestParam int adminId) {
        userService.requireAdmin(adminId);
        inviteCodeService.disableInviteCode(code);
    }

    @DeleteMapping("/{code}")
    public void deleteInviteCode(@PathVariable String code, @RequestParam int adminId) {
        userService.requireAdmin(adminId);
        inviteCodeService.deleteInviteCode(code);
    }

    private InviteCodeResponse toResponse(InviteCode inviteCode) {
        return new InviteCodeResponse(
                inviteCode.getCode(),
                inviteCode.getUsageLimit(),
                inviteCode.getUsedCount(),
                inviteCode.getStatus().name());
    }

    public record GenerateInviteCodeRequest(int adminId, int usageLimit, long expirationTimestampMillis) {
    }

    public record InviteCodeResponse(String code, int usageLimit, int usedCount, String status) {
    }
}
