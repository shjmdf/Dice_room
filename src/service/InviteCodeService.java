package service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import backend.user.InviteCode;
import backend.user.InviteCodeStatus;
import repository.InviteCodeRepository;

/*
 * 邀请码服务类，负责邀请码业务规则。
 *
 * 当前版本已经接入 InviteCodeRepository，不再使用内存 List。
 */
public class InviteCodeService {
    private final InviteCodeRepository inviteCodeRepository;

    public InviteCodeService(InviteCodeRepository inviteCodeRepository) {
        if (inviteCodeRepository == null) {
            throw new IllegalArgumentException("邀请码仓库不能为空");
        }
        this.inviteCodeRepository = inviteCodeRepository;
    }

    public String generateUniqueCode() {
        String uniqueCode = generateCodeText();

        while (exists(uniqueCode)) {
            uniqueCode = generateCodeText();
        }

        return uniqueCode;
    }

    public InviteCode generateInviteCode(int usageLimit, Date expirationDate) {
        return generateInviteCode(usageLimit, expirationDate, null);
    }

    public InviteCode generateInviteCode(int usageLimit, Date expirationDate, Integer createdByUserId) {
        if (usageLimit <= 0) {
            throw new IllegalArgumentException("邀请码使用次数必须大于 0");
        }

        if (expirationDate == null) {
            throw new IllegalArgumentException("邀请码过期时间不能为空");
        }

        String code = generateUniqueCode();
        return inviteCodeRepository.insert(code, usageLimit, expirationDate, createdByUserId);
    }

    public boolean deleteInviteCode(String code) {
        findByCode(code);
        inviteCodeRepository.deleteByCode(code);
        return true;
    }

    public boolean useInviteCode(String code) {
        InviteCode inviteCode = findByCode(code);
        inviteCode.use();
        inviteCodeRepository.updateUsage(code, inviteCode.getUsedCount(), inviteCode.getStatus());
        return true;
    }

    public boolean checkInviteCode(String code) {
        InviteCode inviteCode = findByCodeOrNull(code);
        if (inviteCode == null) {
            return false;
        }

        boolean canUse = inviteCode.canUse();
        if (inviteCode.getStatus() == InviteCodeStatus.EXPIRED) {
            inviteCodeRepository.updateStatus(code, InviteCodeStatus.EXPIRED);
        }
        return canUse;
    }

    public boolean disableInviteCode(String code) {
        InviteCode inviteCode = findByCode(code);
        inviteCode.disable();
        inviteCodeRepository.updateStatus(code, inviteCode.getStatus());
        return true;
    }

    public List<InviteCode> getInviteCodes() {
        return inviteCodeRepository.findAll();
    }

    public InviteCode findByCode(String code) {
        checkCodeText(code);

        InviteCode inviteCode = findByCodeOrNull(code);
        if (inviteCode == null) {
            throw new IllegalArgumentException("邀请码不存在");
        }

        return inviteCode;
    }

    private InviteCode findByCodeOrNull(String code) {
        checkCodeText(code);
        return inviteCodeRepository.findByCode(code);
    }

    private boolean exists(String code) {
        return inviteCodeRepository.findByCode(code) != null;
    }

    private String generateCodeText() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void checkCodeText(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("邀请码不能为空");
        }
    }
}
