package backend.user;

import java.util.Date;

/**
 * 邀请码对象。
 * InviteCode 表示邀请码的str内容，例如 ABC123。
 */
public class InviteCode {
    // 邀请码字符串内容
    private final String code;
    // 使用量上限
    private final int usageLimit;
    // 已使用量
    private int usedCount;
    // 邀请码状态
    private InviteCodeStatus status;
    // 过期时间
    private Date expirationDate;

    public InviteCode(String code, int usageLimit, Date expirationDate) {
        this.code = code;
        this.usageLimit = usageLimit;
        this.usedCount = 0;
        this.status = InviteCodeStatus.ACTIVE;
        this.expirationDate = expirationDate;
    }

    public String getCode() {

        return code;
    }

    public int getUsageLimit() {

        return usageLimit;
    }

    public int getUsedCount() {
        return usedCount;
    }

    public InviteCodeStatus getStatus() {
        return status;
    }

    public boolean canUse() {
        checkExpiration();
        return status == InviteCodeStatus.ACTIVE && usedCount < usageLimit;
    }

    public void use() {
        if (!canUse()) {
            throw new IllegalStateException("邀请码不可使用");
        }
        usedCount++;

        if (usedCount >= usageLimit) {
            status = InviteCodeStatus.USED;
        }
    }

    public void checkExpiration() {
        if (new Date().after(expirationDate)) {
            status = InviteCodeStatus.EXPIRED;
        }
    }

    public boolean isExpired() {
        return status == InviteCodeStatus.EXPIRED;
    }

    public void disable() {
        status = InviteCodeStatus.DISABLED;
    }
}
