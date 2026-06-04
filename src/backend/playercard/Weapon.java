package backend.playercard;

public class Weapon {
    private String name;
    private String skillName;
    private int successRate;
    private String damage;
    private String range;
    private boolean impale;
    private int attacksPerRound;
    private int ammo;
    private String malfunction;

    public Weapon(String name, String skillName, int successRate, String damage, String range,
            boolean impale, int attacksPerRound, int ammo, String malfunction) {
        this.name = name;
        this.skillName = skillName;
        this.successRate = successRate;
        this.damage = damage;
        this.range = range;
        this.impale = impale;
        this.attacksPerRound = attacksPerRound;
        this.ammo = ammo;
        this.malfunction = malfunction;
    }

    public String getName() {
        return name;
    }

    public String getSkillName() {
        return skillName;
    }

    public int getSuccessRate() {
        return successRate;
    }

    public String getDamage() {
        return damage;
    }

    public String getRange() {
        return range;
    }

    public boolean isImpale() {
        return impale;
    }

    public int getAttacksPerRound() {
        return attacksPerRound;
    }

    public int getAmmo() {
        return ammo;
    }

    public String getMalfunction() {
        return malfunction;
    }
}
