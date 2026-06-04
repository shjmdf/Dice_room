package backend.playercard;

import java.util.ArrayList;
import java.util.List;

/**
 * COC 调查员角色卡主对象。
 */
public class PlayerCard {
    private int ownerId;
    private int id;
    private String era;
    private BasicInformation basicInformation;
    private Characteristics characteristics;
    private CharacterStatus characterStatus;
    private SkillSheet skillSheet;
    private List<Weapon> weapons;
    private CombatInfo combatInfo;
    private String possessions;
    private CashAndAssets cashAndAssets;
    private CthulhuMythos cthulhuMythos;
    private Story story;
    private List<Companion> companions;
    private ExperiencedScenarios experiencedScenarios;

    public PlayerCard(int ownerId, int id, String name) {
        this.ownerId = ownerId;
        this.id = id;
        this.era = "";
        this.basicInformation = new BasicInformation(name);
        this.characteristics = new Characteristics();
        this.characterStatus = new CharacterStatus();
        this.skillSheet = new SkillSheet();
        this.weapons = new ArrayList<>();
        this.combatInfo = new CombatInfo();
        this.possessions = "";
        this.cashAndAssets = new CashAndAssets();
        this.cthulhuMythos = new CthulhuMythos();
        this.story = new Story();
        this.companions = new ArrayList<>();
        this.experiencedScenarios = new ExperiencedScenarios();
        addDefaultWeapon();
    }

    private void addDefaultWeapon() {
        weapons.add(new Weapon("徒手格斗", "格斗(斗殴)", 25, "1D3+DB", "接触", false, 1, 0, ""));
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEra() {
        return era;
    }

    public void setEra(String era) {
        this.era = era;
    }

    public String getName() {
        return basicInformation.getName();
    }

    public void setName(String name) {
        basicInformation.setName(name);
    }

    public BasicInformation getBasicInformation() {
        return basicInformation;
    }

    public Characteristics getCharacteristics() {
        return characteristics;
    }

    public CharacterStatus getCharacterStatus() {
        return characterStatus;
    }

    public SkillSheet getSkillSheet() {
        return skillSheet;
    }

    public int getSkill(String skillName) {
        return skillSheet.getSkill(skillName).getSuccessRate();
    }

    public void setSkill(String skillName, int successRate) {
        skillSheet.setSkill(skillName, successRate);
    }

    public List<Weapon> getWeapons() {
        return weapons;
    }

    public void addWeapon(Weapon weapon) {
        weapons.add(weapon);
    }

    public CombatInfo getCombatInfo() {
        return combatInfo;
    }

    public String getPossessions() {
        return possessions;
    }

    public void setPossessions(String possessions) {
        this.possessions = possessions;
    }

    public CashAndAssets getCashAndAssets() {
        return cashAndAssets;
    }

    public CthulhuMythos getCthulhuMythos() {
        return cthulhuMythos;
    }

    public Story getStory() {
        return story;
    }

    public List<Companion> getCompanions() {
        return companions;
    }

    public void addCompanion(Companion companion) {
        companions.add(companion);
    }

    public ExperiencedScenarios getExperiencedScenarios() {
        return experiencedScenarios;
    }

    public void addExperiencedScenario(ExperiencedScenarioEntry experiencedScenario) {
        experiencedScenarios.addEntry(experiencedScenario);
    }
}
