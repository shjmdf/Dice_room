package backend.playercard;

/*
 * 技能
 */
public class Skill {
    // 技能类别，如：战斗、调查、交涉等
    private String category;
    // 技能名称
    private String name;
    // 基础成功率
    private int baseRate;
    // 职业加成
    private int occupationRate;
    // 兴趣加成
    private int interestRate;
    // 成长加成
    private int growthRate;
    // 基本成功率
    private int successRate;
    // 困难成功率
    private int hardRate;
    // 极难成功率
    private int extremeRate;
    // 是否为职业技能
    private boolean occupationSkill;

    public Skill(String category, String name, int baseRate) {
        this.category = category;
        this.name = name;
        this.baseRate = baseRate;
        this.occupationRate = 0;
        this.interestRate = 0;
        this.growthRate = 0;
        this.occupationSkill = false;
        setSuccessRate(baseRate);
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public int getBaseRate() {
        return baseRate;
    }

    public int getOccupationRate() {
        return occupationRate;
    }

    public void setOccupationRate(int occupationRate) {
        CardValueValidator.checkPercentValue(occupationRate);
        this.occupationRate = occupationRate;
        refreshSuccessRate();
    }

    public int getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(int interestRate) {
        CardValueValidator.checkPercentValue(interestRate);
        this.interestRate = interestRate;
        refreshSuccessRate();
    }

    public int getGrowthRate() {
        return growthRate;
    }

    public void setGrowthRate(int growthRate) {
        CardValueValidator.checkPercentValue(growthRate);
        this.growthRate = growthRate;
        refreshSuccessRate();
    }

    public int getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(int successRate) {
        CardValueValidator.checkPercentValue(successRate);
        this.successRate = successRate;
        this.hardRate = Math.max(1, successRate / 2);
        this.extremeRate = Math.max(1, successRate / 5);
    }

    public int getHardRate() {
        return hardRate;
    }

    public int getExtremeRate() {
        return extremeRate;
    }

    public boolean isOccupationSkill() {
        return occupationSkill;
    }

    public void setOccupationSkill(boolean occupationSkill) {
        this.occupationSkill = occupationSkill;
    }

    private void refreshSuccessRate() {
        setSuccessRate(baseRate + occupationRate + interestRate + growthRate);
    }
}
