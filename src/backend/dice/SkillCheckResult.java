package backend.dice;
/*
 * 技能检定结果类，表示一次技能检定的结果。
*/
public class SkillCheckResult {
    private final int userId;
    private final int cardId;
    private final String skillName;
    private final int skillValue;
    private final int hardValue;
    private final int extremeValue;
    private final int roll;
    private final String successLevel;

    public SkillCheckResult(int userId, int cardId, String skillName, int skillValue,
            int hardValue, int extremeValue, int roll, String successLevel) {
        this.userId = userId;
        this.cardId = cardId;
        this.skillName = skillName;
        this.skillValue = skillValue;
        this.hardValue = hardValue;
        this.extremeValue = extremeValue;
        this.roll = roll;
        this.successLevel = successLevel;
    }

    public int getUserId() {
        return userId;
    }

    public int getCardId() {
        return cardId;
    }

    public String getSkillName() {
        return skillName;
    }

    public int getSkillValue() {
        return skillValue;
    }

    public int getHardValue() {
        return hardValue;
    }

    public int getExtremeValue() {
        return extremeValue;
    }

    public int getRoll() {
        return roll;
    }

    public String getSuccessLevel() {
        return successLevel;
    }

    public boolean isSuccess() {
        return "大成功".equals(successLevel)
                || "极难成功".equals(successLevel)
                || "困难成功".equals(successLevel)
                || "普通成功".equals(successLevel);
    }
}
