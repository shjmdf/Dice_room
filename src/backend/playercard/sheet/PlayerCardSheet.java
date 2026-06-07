package backend.playercard.sheet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlayerCardSheet {
    private int version = 1;
    private int id;
    private int ownerId;
    private String era = "";
    private SheetBasicInformation basicInformation = new SheetBasicInformation();
    private Map<String, SheetCharacteristic> characteristics = new LinkedHashMap<>();
    private Map<String, SheetResource> resources = new LinkedHashMap<>();
    private Map<String, Boolean> status = new LinkedHashMap<>();
    private SheetSkillPoints skillPoints = new SheetSkillPoints();
    private List<SheetSkill> skills = new ArrayList<>();
    private List<SheetWeapon> weapons = new ArrayList<>();
    private SheetCombat combat = new SheetCombat();
    private SheetStory story = new SheetStory();
    private SheetPossessions possessions = new SheetPossessions();
    private SheetCashAndAssets cashAndAssets = new SheetCashAndAssets();
    private SheetCthulhuMythos cthulhuMythos = new SheetCthulhuMythos();
    private List<SheetCompanion> relationships = new ArrayList<>();
    private List<SheetExperiencedScenario> experiencedScenarios = new ArrayList<>();

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getEra() {
        return era;
    }

    public void setEra(String era) {
        this.era = era;
    }

    public SheetBasicInformation getBasicInformation() {
        return basicInformation;
    }

    public void setBasicInformation(SheetBasicInformation basicInformation) {
        this.basicInformation = basicInformation;
    }

    public Map<String, SheetCharacteristic> getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(Map<String, SheetCharacteristic> characteristics) {
        this.characteristics = characteristics;
    }

    public Map<String, SheetResource> getResources() {
        return resources;
    }

    public void setResources(Map<String, SheetResource> resources) {
        this.resources = resources;
    }

    public Map<String, Boolean> getStatus() {
        return status;
    }

    public void setStatus(Map<String, Boolean> status) {
        this.status = status;
    }

    public SheetSkillPoints getSkillPoints() {
        return skillPoints;
    }

    public void setSkillPoints(SheetSkillPoints skillPoints) {
        this.skillPoints = skillPoints;
    }

    public List<SheetSkill> getSkills() {
        return skills;
    }

    public void setSkills(List<SheetSkill> skills) {
        this.skills = skills;
    }

    public List<SheetWeapon> getWeapons() {
        return weapons;
    }

    public void setWeapons(List<SheetWeapon> weapons) {
        this.weapons = weapons;
    }

    public SheetCombat getCombat() {
        return combat;
    }

    public void setCombat(SheetCombat combat) {
        this.combat = combat;
    }

    public SheetStory getStory() {
        return story;
    }

    public void setStory(SheetStory story) {
        this.story = story;
    }

    public SheetPossessions getPossessions() {
        return possessions;
    }

    public void setPossessions(SheetPossessions possessions) {
        this.possessions = possessions;
    }

    public SheetCashAndAssets getCashAndAssets() {
        return cashAndAssets;
    }

    public void setCashAndAssets(SheetCashAndAssets cashAndAssets) {
        this.cashAndAssets = cashAndAssets;
    }

    public SheetCthulhuMythos getCthulhuMythos() {
        return cthulhuMythos;
    }

    public void setCthulhuMythos(SheetCthulhuMythos cthulhuMythos) {
        this.cthulhuMythos = cthulhuMythos;
    }

    public List<SheetCompanion> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<SheetCompanion> relationships) {
        this.relationships = relationships;
    }

    public List<SheetExperiencedScenario> getExperiencedScenarios() {
        return experiencedScenarios;
    }

    public void setExperiencedScenarios(List<SheetExperiencedScenario> experiencedScenarios) {
        this.experiencedScenarios = experiencedScenarios;
    }

    public static class SheetBasicInformation {
        private String name = "";
        private String playerName = "";
        private String occupation = "";
        private int age;
        private String gender = "";
        private String sex = "";
        private String residence = "";
        private String birthplace = "";
        private String avatarPath = "";
        private String era = "";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPlayerName() {
            return playerName;
        }

        public void setPlayerName(String playerName) {
            this.playerName = playerName;
        }

        public String getOccupation() {
            return occupation;
        }

        public void setOccupation(String occupation) {
            this.occupation = occupation;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getSex() {
            return sex;
        }

        public void setSex(String sex) {
            this.sex = sex;
        }

        public String getResidence() {
            return residence;
        }

        public void setResidence(String residence) {
            this.residence = residence;
        }

        public String getBirthplace() {
            return birthplace;
        }

        public void setBirthplace(String birthplace) {
            this.birthplace = birthplace;
        }

        public String getAvatarPath() {
            return avatarPath;
        }

        public void setAvatarPath(String avatarPath) {
            this.avatarPath = avatarPath;
        }

        public String getEra() {
            return era;
        }

        public void setEra(String era) {
            this.era = era;
        }
    }

    public static class SheetCharacteristic {
        private String name = "";
        private String code = "";
        private String rollFormula = "";
        private int value;
        private int hard;
        private int extreme;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getRollFormula() {
            return rollFormula;
        }

        public void setRollFormula(String rollFormula) {
            this.rollFormula = rollFormula;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public int getHard() {
            return hard;
        }

        public void setHard(int hard) {
            this.hard = hard;
        }

        public int getExtreme() {
            return extreme;
        }

        public void setExtreme(int extreme) {
            this.extreme = extreme;
        }
    }

    public static class SheetResource {
        private int current;
        private int initial;
        private int max;

        public int getCurrent() {
            return current;
        }

        public void setCurrent(int current) {
            this.current = current;
        }

        public int getInitial() {
            return initial;
        }

        public void setInitial(int initial) {
            this.initial = initial;
        }

        public int getMax() {
            return max;
        }

        public void setMax(int max) {
            this.max = max;
        }
    }

    public static class SheetSkillPoints {
        private int occupationTotal;
        private int occupationRemaining;
        private int interestTotal;
        private int interestRemaining;
        private int occupationLimit = 75;
        private int otherLimit = 50;

        public int getOccupationTotal() {
            return occupationTotal;
        }

        public void setOccupationTotal(int occupationTotal) {
            this.occupationTotal = occupationTotal;
        }

        public int getOccupationRemaining() {
            return occupationRemaining;
        }

        public void setOccupationRemaining(int occupationRemaining) {
            this.occupationRemaining = occupationRemaining;
        }

        public int getInterestTotal() {
            return interestTotal;
        }

        public void setInterestTotal(int interestTotal) {
            this.interestTotal = interestTotal;
        }

        public int getInterestRemaining() {
            return interestRemaining;
        }

        public void setInterestRemaining(int interestRemaining) {
            this.interestRemaining = interestRemaining;
        }

        public int getOccupationLimit() {
            return occupationLimit;
        }

        public void setOccupationLimit(int occupationLimit) {
            this.occupationLimit = occupationLimit;
        }

        public int getOtherLimit() {
            return otherLimit;
        }

        public void setOtherLimit(int otherLimit) {
            this.otherLimit = otherLimit;
        }
    }

    public static class SheetSkill {
        private String category = "";
        private String name = "";
        private String specialization = "";
        private int base;
        private int occupation;
        private int interest;
        private int growth;
        private int success;
        private int hard;
        private int extreme;
        private boolean occupationSkill;

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSpecialization() {
            return specialization;
        }

        public void setSpecialization(String specialization) {
            this.specialization = specialization;
        }

        public int getBase() {
            return base;
        }

        public void setBase(int base) {
            this.base = base;
        }

        public int getOccupation() {
            return occupation;
        }

        public void setOccupation(int occupation) {
            this.occupation = occupation;
        }

        public int getInterest() {
            return interest;
        }

        public void setInterest(int interest) {
            this.interest = interest;
        }

        public int getGrowth() {
            return growth;
        }

        public void setGrowth(int growth) {
            this.growth = growth;
        }

        public int getSuccess() {
            return success;
        }

        public void setSuccess(int success) {
            this.success = success;
        }

        public int getHard() {
            return hard;
        }

        public void setHard(int hard) {
            this.hard = hard;
        }

        public int getExtreme() {
            return extreme;
        }

        public void setExtreme(int extreme) {
            this.extreme = extreme;
        }

        public boolean isOccupationSkill() {
            return occupationSkill;
        }

        public void setOccupationSkill(boolean occupationSkill) {
            this.occupationSkill = occupationSkill;
        }
    }

    public static class SheetWeapon {
        private String name = "";
        private String skill = "";
        private int successRate;
        private String damage = "";
        private String range = "";
        private boolean impale;
        private int attacksPerRound;
        private int ammo;
        private String malfunction = "";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSkill() {
            return skill;
        }

        public void setSkill(String skill) {
            this.skill = skill;
        }

        public int getSuccessRate() {
            return successRate;
        }

        public void setSuccessRate(int successRate) {
            this.successRate = successRate;
        }

        public String getDamage() {
            return damage;
        }

        public void setDamage(String damage) {
            this.damage = damage;
        }

        public String getRange() {
            return range;
        }

        public void setRange(String range) {
            this.range = range;
        }

        public boolean isImpale() {
            return impale;
        }

        public void setImpale(boolean impale) {
            this.impale = impale;
        }

        public int getAttacksPerRound() {
            return attacksPerRound;
        }

        public void setAttacksPerRound(int attacksPerRound) {
            this.attacksPerRound = attacksPerRound;
        }

        public int getAmmo() {
            return ammo;
        }

        public void setAmmo(int ammo) {
            this.ammo = ammo;
        }

        public String getMalfunction() {
            return malfunction;
        }

        public void setMalfunction(String malfunction) {
            this.malfunction = malfunction;
        }
    }

    public static class SheetCombat {
        private String damageBonus = "0";
        private int build;
        private int armor;
        private int movement;

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
            this.armor = armor;
        }

        public int getMovement() {
            return movement;
        }

        public void setMovement(int movement) {
            this.movement = movement;
        }
    }

    public static class SheetStory {
        private String appearance = "";
        private String ideologyAndBeliefs = "";
        private String significantPeople = "";
        private String meaningfulLocations = "";
        private String treasuredPossessions = "";
        private String traits = "";
        private String injuriesAndScars = "";
        private String mentalSymptoms = "";
        private String personalDescription = "";

        public String getAppearance() {
            return appearance;
        }

        public void setAppearance(String appearance) {
            this.appearance = appearance;
        }

        public String getIdeologyAndBeliefs() {
            return ideologyAndBeliefs;
        }

        public void setIdeologyAndBeliefs(String ideologyAndBeliefs) {
            this.ideologyAndBeliefs = ideologyAndBeliefs;
        }

        public String getSignificantPeople() {
            return significantPeople;
        }

        public void setSignificantPeople(String significantPeople) {
            this.significantPeople = significantPeople;
        }

        public String getMeaningfulLocations() {
            return meaningfulLocations;
        }

        public void setMeaningfulLocations(String meaningfulLocations) {
            this.meaningfulLocations = meaningfulLocations;
        }

        public String getTreasuredPossessions() {
            return treasuredPossessions;
        }

        public void setTreasuredPossessions(String treasuredPossessions) {
            this.treasuredPossessions = treasuredPossessions;
        }

        public String getTraits() {
            return traits;
        }

        public void setTraits(String traits) {
            this.traits = traits;
        }

        public String getInjuriesAndScars() {
            return injuriesAndScars;
        }

        public void setInjuriesAndScars(String injuriesAndScars) {
            this.injuriesAndScars = injuriesAndScars;
        }

        public String getMentalSymptoms() {
            return mentalSymptoms;
        }

        public void setMentalSymptoms(String mentalSymptoms) {
            this.mentalSymptoms = mentalSymptoms;
        }

        public String getPersonalDescription() {
            return personalDescription;
        }

        public void setPersonalDescription(String personalDescription) {
            this.personalDescription = personalDescription;
        }
    }

    public static class SheetPossessions {
        private String text = "";
        private List<SheetItem> items = new ArrayList<>();

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public List<SheetItem> getItems() {
            return items;
        }

        public void setItems(List<SheetItem> items) {
            this.items = items;
        }
    }

    public static class SheetItem {
        private String name = "";
        private int count = 1;
        private String description = "";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class SheetCashAndAssets {
        private int creditRating;
        private String cash = "";
        private String spendingLevel = "";
        private String assets = "";
        private String details = "";

        public int getCreditRating() {
            return creditRating;
        }

        public void setCreditRating(int creditRating) {
            this.creditRating = creditRating;
        }

        public String getCash() {
            return cash;
        }

        public void setCash(String cash) {
            this.cash = cash;
        }

        public String getSpendingLevel() {
            return spendingLevel;
        }

        public void setSpendingLevel(String spendingLevel) {
            this.spendingLevel = spendingLevel;
        }

        public String getAssets() {
            return assets;
        }

        public void setAssets(String assets) {
            this.assets = assets;
        }

        public String getDetails() {
            return details;
        }

        public void setDetails(String details) {
            this.details = details;
        }
    }

    public static class SheetCthulhuMythos {
        private int mythosSkillValue;
        private String magicItemsAndTomes = "";
        private String spells = "";
        private String thirdContact = "";
        private List<SheetCthulhuMythosEntry> entries = new ArrayList<>();

        public int getMythosSkillValue() {
            return mythosSkillValue;
        }

        public void setMythosSkillValue(int mythosSkillValue) {
            this.mythosSkillValue = mythosSkillValue;
        }

        public String getMagicItemsAndTomes() {
            return magicItemsAndTomes;
        }

        public void setMagicItemsAndTomes(String magicItemsAndTomes) {
            this.magicItemsAndTomes = magicItemsAndTomes;
        }

        public String getSpells() {
            return spells;
        }

        public void setSpells(String spells) {
            this.spells = spells;
        }

        public String getThirdContact() {
            return thirdContact;
        }

        public void setThirdContact(String thirdContact) {
            this.thirdContact = thirdContact;
        }

        public List<SheetCthulhuMythosEntry> getEntries() {
            return entries;
        }

        public void setEntries(List<SheetCthulhuMythosEntry> entries) {
            this.entries = entries;
        }
    }

    public static class SheetCthulhuMythosEntry {
        private String entryType = "";
        private String name = "";
        private String description = "";
        private String source = "";

        public String getEntryType() {
            return entryType;
        }

        public void setEntryType(String entryType) {
            this.entryType = entryType;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }
    }

    public static class SheetCompanion {
        private String characterName = "";
        private String relationship = "";
        private String playerName = "";

        public String getCharacterName() {
            return characterName;
        }

        public void setCharacterName(String characterName) {
            this.characterName = characterName;
        }

        public String getRelationship() {
            return relationship;
        }

        public void setRelationship(String relationship) {
            this.relationship = relationship;
        }

        public String getPlayerName() {
            return playerName;
        }

        public void setPlayerName(String playerName) {
            this.playerName = playerName;
        }
    }

    public static class SheetExperiencedScenario {
        private String scenarioName = "";
        private String experience = "";

        public String getScenarioName() {
            return scenarioName;
        }

        public void setScenarioName(String scenarioName) {
            this.scenarioName = scenarioName;
        }

        public String getExperience() {
            return experience;
        }

        public void setExperience(String experience) {
            this.experience = experience;
        }
    }
}
