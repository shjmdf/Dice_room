package backend.playercard.sheet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import backend.playercard.BasicInformation;
import backend.playercard.CashAndAssets;
import backend.playercard.CharacterStatus;
import backend.playercard.Characteristic;
import backend.playercard.Characteristics;
import backend.playercard.CombatInfo;
import backend.playercard.Companion;
import backend.playercard.CthulhuMythos;
import backend.playercard.CthulhuMythosEntry;
import backend.playercard.ExperiencedScenarioEntry;
import backend.playercard.ExperiencedScenarios;
import backend.playercard.PlayerCard;
import backend.playercard.ResourceValue;
import backend.playercard.Skill;
import backend.playercard.SkillSheet;
import backend.playercard.Story;
import backend.playercard.Weapon;
import backend.playercard.sheet.PlayerCardSheet.SheetBasicInformation;
import backend.playercard.sheet.PlayerCardSheet.SheetCashAndAssets;
import backend.playercard.sheet.PlayerCardSheet.SheetCharacteristic;
import backend.playercard.sheet.PlayerCardSheet.SheetCombat;
import backend.playercard.sheet.PlayerCardSheet.SheetCompanion;
import backend.playercard.sheet.PlayerCardSheet.SheetCthulhuMythos;
import backend.playercard.sheet.PlayerCardSheet.SheetCthulhuMythosEntry;
import backend.playercard.sheet.PlayerCardSheet.SheetExperiencedScenario;
import backend.playercard.sheet.PlayerCardSheet.SheetItem;
import backend.playercard.sheet.PlayerCardSheet.SheetPossessions;
import backend.playercard.sheet.PlayerCardSheet.SheetResource;
import backend.playercard.sheet.PlayerCardSheet.SheetSkill;
import backend.playercard.sheet.PlayerCardSheet.SheetSkillPoints;
import backend.playercard.sheet.PlayerCardSheet.SheetStory;
import backend.playercard.sheet.PlayerCardSheet.SheetWeapon;

public class PlayerCardSheetMapper {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private PlayerCardSheetMapper() {
    }

    public static PlayerCardSheet fromPlayerCard(PlayerCard card) {
        PlayerCardSheet sheet = new PlayerCardSheet();
        sheet.setVersion(1);
        sheet.setId(card.getId());
        sheet.setOwnerId(card.getOwnerId());
        sheet.setEra(text(card.getEra()));
        sheet.setBasicInformation(basicInformation(card));
        sheet.setCharacteristics(characteristics(card.getCharacteristics()));
        sheet.setResources(resources(card.getCharacteristics()));
        sheet.setStatus(status(card.getCharacterStatus()));
        sheet.setSkillPoints(skillPoints(card.getSkillSheet()));
        sheet.setSkills(skills(card.getSkillSheet()));
        sheet.setWeapons(weapons(card.getWeapons()));
        sheet.setCombat(combat(card.getCombatInfo()));
        sheet.setStory(story(card.getStory()));
        sheet.setPossessions(possessions(card.getPossessions()));
        sheet.setCashAndAssets(cashAndAssets(card.getCashAndAssets()));
        sheet.setCthulhuMythos(cthulhuMythos(card.getCthulhuMythos()));
        sheet.setRelationships(companions(card.getCompanions()));
        sheet.setExperiencedScenarios(experiencedScenarios(card.getExperiencedScenarios()));
        return sheet;
    }

    public static PlayerCardSheet fromJson(String cardJson) {
        if (cardJson == null || cardJson.isBlank()) {
            throw new IllegalArgumentException("角色卡 JSON 不能为空");
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(cardJson);
            migrateLegacyCharacteristics(root);
            PlayerCardSheet sheet = OBJECT_MAPPER.treeToValue(root, PlayerCardSheet.class);
            normalize(sheet);
            return sheet;
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("角色卡 JSON 格式错误", exception);
        }
    }

    public static String toJson(PlayerCardSheet sheet) {
        if (sheet == null) {
            throw new IllegalArgumentException("角色卡内容不能为空");
        }

        normalize(sheet);

        try {
            return OBJECT_MAPPER.writeValueAsString(sheet);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("生成角色卡 JSON 失败", exception);
        }
    }

    public static void normalizeIdentity(PlayerCardSheet sheet, int userId, int cardId) {
        normalize(sheet);
        sheet.setOwnerId(userId);
        sheet.setId(cardId);
    }

    public static String getSummaryName(PlayerCardSheet sheet, String fallbackName) {
        normalize(sheet);
        String name = sheet.getBasicInformation().getName();
        if (name == null || name.isBlank()) {
            return fallbackName;
        }
        return name;
    }

    public static String getSummaryEra(PlayerCardSheet sheet) {
        normalize(sheet);
        String era = sheet.getEra();
        if (era == null || era.isBlank()) {
            era = sheet.getBasicInformation().getEra();
        }
        return text(era);
    }

    public static SheetSkill requireSkill(PlayerCardSheet sheet, String skillName) {
        normalize(sheet);
        if (skillName == null || skillName.isBlank()) {
            throw new IllegalArgumentException("技能名称不能为空");
        }

        String normalizedName = normalizeSkillName(skillName);
        for (SheetSkill skill : sheet.getSkills()) {
            if (skill == null) {
                continue;
            }

            String fullName = normalizeSkillName(fullSkillName(skill));
            String simpleName = normalizeSkillName(skill.getName());
            if (normalizedName.equals(fullName) || normalizedName.equals(simpleName)) {
                return skill;
            }
        }

        throw new IllegalArgumentException("角色卡中不存在技能：" + skillName);
    }

    public static SkillValue toSkillValue(SheetSkill skill) {
        int success = skillSuccess(skill);
        int hard = skill.getHard() > 0 ? skill.getHard() : Math.max(1, success / 2);
        int extreme = skill.getExtreme() > 0 ? skill.getExtreme() : Math.max(1, success / 5);
        return new SkillValue(fullSkillName(skill), success, hard, extreme);
    }

    private static void normalize(PlayerCardSheet sheet) {
        if (sheet == null) {
            throw new IllegalArgumentException("角色卡内容不能为空");
        }
        if (sheet.getBasicInformation() == null) {
            sheet.setBasicInformation(new SheetBasicInformation());
        }
        if (sheet.getCharacteristics() == null) {
            sheet.setCharacteristics(new LinkedHashMap<>());
        }
        if (sheet.getResources() == null) {
            sheet.setResources(new LinkedHashMap<>());
        }
        if (sheet.getStatus() == null) {
            sheet.setStatus(new LinkedHashMap<>());
        }
        if (sheet.getSkillPoints() == null) {
            sheet.setSkillPoints(new SheetSkillPoints());
        }
        if (sheet.getSkills() == null) {
            sheet.setSkills(new ArrayList<>());
        }
        if (sheet.getWeapons() == null) {
            sheet.setWeapons(new ArrayList<>());
        }
        if (sheet.getCombat() == null) {
            sheet.setCombat(new SheetCombat());
        }
        if (sheet.getStory() == null) {
            sheet.setStory(new SheetStory());
        }
        if (sheet.getPossessions() == null) {
            sheet.setPossessions(new SheetPossessions());
        }
        if (sheet.getPossessions().getItems() == null) {
            sheet.getPossessions().setItems(new ArrayList<>());
        }
        if (sheet.getCashAndAssets() == null) {
            sheet.setCashAndAssets(new SheetCashAndAssets());
        }
        if (sheet.getCthulhuMythos() == null) {
            sheet.setCthulhuMythos(new SheetCthulhuMythos());
        }
        if (sheet.getCthulhuMythos().getEntries() == null) {
            sheet.getCthulhuMythos().setEntries(new ArrayList<>());
        }
        if (sheet.getRelationships() == null) {
            sheet.setRelationships(new ArrayList<>());
        }
        if (sheet.getExperiencedScenarios() == null) {
            sheet.setExperiencedScenarios(new ArrayList<>());
        }
    }

    private static void migrateLegacyCharacteristics(JsonNode root) {
        JsonNode characteristics = root.get("characteristics");
        if (!(characteristics instanceof ObjectNode characteristicsObject)) {
            return;
        }

        migrateLegacyCharacteristic(characteristicsObject, "str", "力量", "STR", "3d6*5");
        migrateLegacyCharacteristic(characteristicsObject, "dex", "敏捷", "DEX", "3d6*5");
        migrateLegacyCharacteristic(characteristicsObject, "siz", "体型", "SIZ", "(2d6+6)*5");
        migrateLegacyCharacteristic(characteristicsObject, "app", "外貌", "APP", "3d6*5");
        migrateLegacyCharacteristic(characteristicsObject, "con", "体质", "CON", "3d6*5");
        migrateLegacyCharacteristic(characteristicsObject, "int", "智力", "INT", "(2d6+6)*5");
        migrateLegacyCharacteristic(characteristicsObject, "pow", "意志", "POW", "3d6*5");
        migrateLegacyCharacteristic(characteristicsObject, "edu", "教育", "EDU", "(2d6+6)*5");
        migrateLegacyCharacteristic(characteristicsObject, "luck", "幸运", "LUC", "3d6*5");
    }

    private static void migrateLegacyCharacteristic(ObjectNode parent, String key, String name, String code,
            String formula) {
        JsonNode value = parent.get(key);
        if (value == null || !value.isNumber()) {
            return;
        }

        int score = value.asInt();
        ObjectNode migrated = OBJECT_MAPPER.createObjectNode();
        migrated.put("name", name);
        migrated.put("code", code);
        migrated.put("rollFormula", formula);
        migrated.put("value", score);
        migrated.put("hard", Math.max(1, score / 2));
        migrated.put("extreme", Math.max(1, score / 5));
        parent.set(key, migrated);
    }

    private static SheetBasicInformation basicInformation(PlayerCard card) {
        BasicInformation basic = card.getBasicInformation();
        SheetBasicInformation sheetBasic = new SheetBasicInformation();
        sheetBasic.setName(text(basic.getName()));
        sheetBasic.setOccupation(text(basic.getOccupation()));
        sheetBasic.setAge(basic.getAge());
        sheetBasic.setGender(text(basic.getGender()));
        sheetBasic.setSex(text(basic.getGender()));
        sheetBasic.setResidence(text(basic.getResidence()));
        sheetBasic.setBirthplace(text(basic.getBirthplace()));
        sheetBasic.setAvatarPath(text(basic.getAvatarPath()));
        sheetBasic.setEra(text(card.getEra()));
        return sheetBasic;
    }

    private static Map<String, SheetCharacteristic> characteristics(Characteristics characteristics) {
        Map<String, SheetCharacteristic> values = new LinkedHashMap<>();
        values.put("str", characteristic(characteristics.getStr()));
        values.put("dex", characteristic(characteristics.getDex()));
        values.put("siz", characteristic(characteristics.getSiz()));
        values.put("app", characteristic(characteristics.getApp()));
        values.put("con", characteristic(characteristics.getCon()));
        values.put("int", characteristic(characteristics.getIntel()));
        values.put("pow", characteristic(characteristics.getPow()));
        values.put("edu", characteristic(characteristics.getEdu()));
        values.put("luck", characteristic(characteristics.getLuck()));
        return values;
    }

    private static SheetCharacteristic characteristic(Characteristic characteristic) {
        SheetCharacteristic sheetCharacteristic = new SheetCharacteristic();
        sheetCharacteristic.setName(text(characteristic.getName()));
        sheetCharacteristic.setCode(text(characteristic.getCode()));
        sheetCharacteristic.setRollFormula(text(characteristic.getRollFormula()));
        sheetCharacteristic.setValue(characteristic.getValue());
        sheetCharacteristic.setHard(characteristic.getHardValue());
        sheetCharacteristic.setExtreme(characteristic.getExtremeValue());
        return sheetCharacteristic;
    }

    private static Map<String, SheetResource> resources(Characteristics characteristics) {
        Map<String, SheetResource> values = new LinkedHashMap<>();
        values.put("hp", resource(characteristics.getHp()));
        values.put("mp", resource(characteristics.getMp()));
        values.put("san", resource(characteristics.getSan()));
        return values;
    }

    private static SheetResource resource(ResourceValue value) {
        SheetResource sheetResource = new SheetResource();
        sheetResource.setCurrent(value.getCurrent());
        sheetResource.setInitial(value.getInitial());
        sheetResource.setMax(value.getMax());
        return sheetResource;
    }

    private static Map<String, Boolean> status(CharacterStatus status) {
        Map<String, Boolean> values = new LinkedHashMap<>();
        values.put("seriousInjury", status.isSeriousInjury());
        values.put("majorWound", status.isSeriousInjury());
        values.put("unconscious", status.isUnconscious());
        values.put("dead", status.isDead());
        values.put("temporaryInsanity", status.isTemporaryInsanity());
        values.put("indefiniteInsanity", status.isIndefiniteInsanity());
        values.put("permanentInsanity", status.isPermanentInsanity());
        return values;
    }

    private static SheetSkillPoints skillPoints(SkillSheet skillSheet) {
        SheetSkillPoints points = new SheetSkillPoints();
        points.setOccupationTotal(skillSheet.getOccupationPoints());
        points.setOccupationRemaining(skillSheet.getRemainingOccupationPoints());
        points.setInterestTotal(skillSheet.getInterestPoints());
        points.setInterestRemaining(skillSheet.getRemainingInterestPoints());
        points.setOccupationLimit(skillSheet.getOccupationSkillLimit());
        points.setOtherLimit(skillSheet.getOtherSkillLimit());
        return points;
    }

    private static List<SheetSkill> skills(SkillSheet skillSheet) {
        List<SheetSkill> rows = new ArrayList<>();
        addExistingSkillRows(rows, skillSheet);
        addSpecializationRows(rows);
        return rows;
    }

    private static void addExistingSkillRows(List<SheetSkill> rows, SkillSheet skillSheet) {
        List<String> order = List.of(
                "信用评级", "克苏鲁神话",
                "侦查", "聆听", "图书馆使用", "计算机使用Ω", "潜行", "追踪", "导航", "读唇",
                "话术", "说服", "取悦", "恐吓", "心理学", "母语", "外语",
                "闪避", "格斗(斗殴)", "射击(手枪)", "射击(步/霰)", "投掷", "爆破", "炮术",
                "急救", "医学", "精神分析", "催眠",
                "攀爬", "跳跃", "游泳", "潜水",
                "博物学", "神秘学", "考古学", "人类学", "估价", "会计", "法律", "历史",
                "电子学Ω", "科学",
                "乔装", "妙手", "锁匠", "机械维修", "电气维修", "驯兽", "技艺", "生存",
                "汽车驾驶", "骑术", "驾驶", "操作重型机械");

        for (String skillName : order) {
            Skill skill = skillSheet.getSkills().get(skillName);
            if (skill != null) {
                rows.add(skill(skill));
            }
        }

        for (Skill skill : skillSheet.getSkills().values()) {
            if (!order.contains(skill.getName())) {
                rows.add(skill(skill));
            }
        }
    }

    private static void addSpecializationRows(List<SheetSkill> rows) {
        rows.add(blankSkill("语言", "外语", "", 1));
        rows.add(blankSkill("战斗", "格斗", "", 0));
        rows.add(blankSkill("战斗", "格斗", "", 0));
        rows.add(blankSkill("战斗", "射击", "", 0));
        rows.add(blankSkill("知识", "科学", "", 1));
        rows.add(blankSkill("知识", "科学", "", 1));
        rows.add(blankSkill("其它", "技艺", "", 5));
        rows.add(blankSkill("其它", "技艺", "", 5));
        rows.add(blankSkill("其它", "生存", "", 5));
        rows.add(blankSkill("其它", "生存", "", 5));
        rows.add(blankSkill("其它", "自定义", "", 0));
        rows.add(blankSkill("其它", "自定义", "", 0));
        rows.add(blankSkill("其它", "自定义", "", 0));
    }

    private static SheetSkill skill(Skill skill) {
        SkillNameParts nameParts = splitSkillName(skill.getName());
        SheetSkill sheetSkill = blankSkill(skill.getCategory(), nameParts.name(), nameParts.specialization(),
                skill.getBaseRate());
        sheetSkill.setOccupation(skill.getOccupationRate());
        sheetSkill.setInterest(skill.getInterestRate());
        sheetSkill.setGrowth(skill.getGrowthRate());
        sheetSkill.setSuccess(skill.getSuccessRate());
        sheetSkill.setHard(skill.getHardRate());
        sheetSkill.setExtreme(skill.getExtremeRate());
        sheetSkill.setOccupationSkill(skill.isOccupationSkill());
        return sheetSkill;
    }

    private static SheetSkill blankSkill(String category, String name, String specialization, int base) {
        SheetSkill skill = new SheetSkill();
        skill.setCategory(text(category));
        skill.setName(text(name));
        skill.setSpecialization(text(specialization));
        skill.setBase(base);
        skill.setSuccess(base);
        skill.setHard(Math.max(1, base / 2));
        skill.setExtreme(Math.max(1, base / 5));
        return skill;
    }

    private static List<SheetWeapon> weapons(List<Weapon> weapons) {
        List<SheetWeapon> rows = new ArrayList<>();
        for (Weapon weapon : weapons) {
            SheetWeapon sheetWeapon = new SheetWeapon();
            sheetWeapon.setName(text(weapon.getName()));
            sheetWeapon.setSkill(text(weapon.getSkillName()));
            sheetWeapon.setSuccessRate(weapon.getSuccessRate());
            sheetWeapon.setDamage(text(weapon.getDamage()));
            sheetWeapon.setRange(text(weapon.getRange()));
            sheetWeapon.setImpale(weapon.isImpale());
            sheetWeapon.setAttacksPerRound(weapon.getAttacksPerRound());
            sheetWeapon.setAmmo(weapon.getAmmo());
            sheetWeapon.setMalfunction(text(weapon.getMalfunction()));
            rows.add(sheetWeapon);
        }

        while (rows.size() < 5) {
            rows.add(new SheetWeapon());
        }
        return rows;
    }

    private static SheetCombat combat(CombatInfo combatInfo) {
        SheetCombat combat = new SheetCombat();
        combat.setDamageBonus(text(combatInfo.getDamageBonus()));
        combat.setBuild(combatInfo.getBuild());
        combat.setArmor(combatInfo.getArmor());
        combat.setMovement(combatInfo.getMoveRate());
        return combat;
    }

    private static SheetStory story(Story story) {
        SheetStory sheetStory = new SheetStory();
        sheetStory.setAppearance(text(story.getAppearance()));
        sheetStory.setIdeologyAndBeliefs(text(story.getIdeologyAndBeliefs()));
        sheetStory.setSignificantPeople(text(story.getSignificantPeople()));
        sheetStory.setMeaningfulLocations(text(story.getMeaningfulLocations()));
        sheetStory.setTreasuredPossessions(text(story.getTreasuredPossessions()));
        sheetStory.setTraits(text(story.getTraits()));
        sheetStory.setInjuriesAndScars(text(story.getInjuriesAndScars()));
        sheetStory.setMentalSymptoms(text(story.getMentalSymptoms()));
        sheetStory.setPersonalDescription(text(story.getPersonalDescription()));
        return sheetStory;
    }

    private static SheetPossessions possessions(String possessions) {
        SheetPossessions sheetPossessions = new SheetPossessions();
        sheetPossessions.setText(text(possessions));
        while (sheetPossessions.getItems().size() < 8) {
            sheetPossessions.getItems().add(new SheetItem());
        }
        return sheetPossessions;
    }

    private static SheetCashAndAssets cashAndAssets(CashAndAssets cashAndAssets) {
        SheetCashAndAssets sheetCashAndAssets = new SheetCashAndAssets();
        sheetCashAndAssets.setCreditRating(cashAndAssets.getCreditRating());
        sheetCashAndAssets.setCash(text(cashAndAssets.getCash()));
        sheetCashAndAssets.setSpendingLevel(text(cashAndAssets.getSpendingLevel()));
        sheetCashAndAssets.setAssets(text(cashAndAssets.getAssets()));
        return sheetCashAndAssets;
    }

    private static SheetCthulhuMythos cthulhuMythos(CthulhuMythos cthulhuMythos) {
        SheetCthulhuMythos sheetCthulhuMythos = new SheetCthulhuMythos();
        sheetCthulhuMythos.setMythosSkillValue(cthulhuMythos.getMythosSkillValue());
        for (CthulhuMythosEntry entry : cthulhuMythos.getEntries()) {
            SheetCthulhuMythosEntry sheetEntry = new SheetCthulhuMythosEntry();
            sheetEntry.setEntryType(text(entry.getEntryType()));
            sheetEntry.setName(text(entry.getName()));
            sheetEntry.setDescription(text(entry.getDescription()));
            sheetEntry.setSource(text(entry.getSource()));
            sheetCthulhuMythos.getEntries().add(sheetEntry);
        }

        while (sheetCthulhuMythos.getEntries().size() < 6) {
            sheetCthulhuMythos.getEntries().add(new SheetCthulhuMythosEntry());
        }
        return sheetCthulhuMythos;
    }

    private static List<SheetCompanion> companions(List<Companion> companions) {
        List<SheetCompanion> rows = new ArrayList<>();
        for (Companion companion : companions) {
            SheetCompanion sheetCompanion = new SheetCompanion();
            sheetCompanion.setCharacterName(text(companion.getCharacterName()));
            sheetCompanion.setRelationship(text(companion.getRelationship()));
            sheetCompanion.setPlayerName(text(companion.getPlayerName()));
            rows.add(sheetCompanion);
        }

        while (rows.size() < 8) {
            rows.add(new SheetCompanion());
        }
        return rows;
    }

    private static List<SheetExperiencedScenario> experiencedScenarios(ExperiencedScenarios experiencedScenarios) {
        List<SheetExperiencedScenario> rows = new ArrayList<>();
        for (ExperiencedScenarioEntry entry : experiencedScenarios.getEntries()) {
            SheetExperiencedScenario sheetEntry = new SheetExperiencedScenario();
            sheetEntry.setScenarioName(text(entry.getScenarioName()));
            sheetEntry.setExperience(text(entry.getExperience()));
            rows.add(sheetEntry);
        }

        while (rows.size() < 8) {
            rows.add(new SheetExperiencedScenario());
        }
        return rows;
    }

    private static int skillSuccess(SheetSkill skill) {
        int success = skill.getSuccess();
        if (success > 0) {
            return success;
        }
        return skill.getBase() + skill.getOccupation() + skill.getInterest() + skill.getGrowth();
    }

    private static String fullSkillName(SheetSkill skill) {
        String name = text(skill.getName());
        String specialization = text(skill.getSpecialization());
        if (specialization.isBlank()) {
            return name;
        }
        return name + "(" + specialization + ")";
    }

    private static SkillNameParts splitSkillName(String skillName) {
        if (skillName == null) {
            return new SkillNameParts("", "");
        }

        int left = skillName.indexOf('(');
        int right = skillName.endsWith(")") ? skillName.length() - 1 : -1;
        if (left > 0 && right > left) {
            return new SkillNameParts(skillName.substring(0, left), skillName.substring(left + 1, right));
        }
        return new SkillNameParts(skillName, "");
    }

    private static String normalizeSkillName(String value) {
        return text(value).replace("（", "(").replace("）", ")").replaceAll("\\s+", "").toLowerCase();
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }

    private record SkillNameParts(String name, String specialization) {
    }

    public record SkillValue(String skillName, int successRate, int hardRate, int extremeRate) {
    }
}
