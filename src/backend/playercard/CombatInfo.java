package backend.playercard;

/*
 * 角色战斗信息。
 *
 * 包含伤害加值、体型、护甲和移动力等字段。
 */
public class CombatInfo {
    private String damageBonus;
    private int build;
    private int armor;
    private int moveRate;

    public CombatInfo() {
        this.damageBonus = "0";
        this.build = 0;
        this.armor = 0;
        this.moveRate = 0;
    }

    public String getDamageBonus() {
        return damageBonus;
    }

    public void setDamageBonus(String damageBonus) {
        this.damageBonus = damageBonus;
    }

    public int getBuild() {
        return build;
    }

    public void setBuild(int build) {
        this.build = build;
    }

    public int getArmor() {
        return armor;
    }

    public void setArmor(int armor) {
        CardValueValidator.checkNonNegative(armor, "护甲");
        this.armor = armor;
    }

    public int getMoveRate() {
        return moveRate;
    }

    public void setMoveRate(int moveRate) {
        CardValueValidator.checkNonNegative(moveRate, "移动力");
        this.moveRate = moveRate;
    }
}
