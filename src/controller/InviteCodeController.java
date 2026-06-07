package controller;

import java.util.Date;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import backend.user.InviteCode;
import backend.user.User;
import service.AuthService;
import service.InviteCodeService;

@RestController
@RequestMapping("/api/invite-codes")
public class InviteCodeController {
    private final InviteCodeService inviteCodeService;
    private final AuthService authService;

    public InviteCodeController(InviteCodeService inviteCodeService, AuthService authService) {
        this.inviteCodeService = inviteCodeService;
        this.authService = authService;
    }

    @PostMapping
    public InviteCodeResponse generateInviteCode(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @RequestBody GenerateInviteCodeRequest request) {
        User admin = authService.requireAdmin(authorization);
        InviteCode inviteCode = inviteCodeService.generateInviteCode(
                request.usageLimit(),
                new Date(request.expirationTimestampMillis()),
                admin.getId());
        return toResponse(inviteCode);
    }

    @GetMapping
    public List<InviteCodeResponse> getInviteCodes(
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        authService.requireAdmin(authorization);
        return inviteCodeService.getInviteCodes().stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/{code}/disable")
    public void disableInviteCode(
            @PathVariable String code,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        authService.requireAdmin(authorization);
        inviteCodeService.disableInviteCode(code);
    }

    @DeleteMapping("/{code}")
    public void deleteInviteCode(
            @PathVariable String code,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        authService.requireAdmin(authorization);
        inviteCodeService.deleteInviteCode(code);
    }

    private InviteCodeResponse toResponse(InviteCode inviteCode) {
        return new InviteCodeResponse(
                inviteCode.getCode(),
                inviteCode.getUsageLimit(),
                inviteCode.getUsedCount(),
                inviteCode.getStatus().name(),
                inviteCode.getExpirationDate().getTime());
    }

    public record GenerateInviteCodeRequest(int usageLimit, long expirationTimestampMillis) {
    }

    public record InviteCodeResponse(String code, int usageLimit, int usedCount, String status, long expirationTimestampMillis) {
    }
}
