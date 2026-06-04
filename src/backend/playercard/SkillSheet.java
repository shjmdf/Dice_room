package backend.playercard;

import java.util.HashMap;
import java.util.Map;

public class SkillSheet {
    private int occupationPoints;
    private int remainingOccupationPoints;
    private int interestPoints;
    private int remainingInterestPoints;
    private int occupationSkillLimit;
    private int otherSkillLimit;
    private Map<String, Skill> skills;

    public SkillSheet() {
        this.occupationPoints = 0;
        this.remainingOccupationPoints = 0;
        this.interestPoints = 0;
        this.remainingInterestPoints = 0;
        this.occupationSkillLimit = 75;
        this.otherSkillLimit = 50;
        this.skills = new HashMap<>();
        initDefaultSkills();
    }

    private void initDefaultSkills() {
        addSkill(new Skill("特殊", "信用评级", 0));
        addSkill(new Skill("特殊", "克苏鲁神话", 0));
        addSkill(new Skill("探索", "侦查", 25));
        addSkill(new Skill("探索", "聆听", 20));
        addSkill(new Skill("探索", "潜行", 20));
        addSkill(new Skill("探索", "追踪", 10));
        addSkill(new Skill("探索", "读唇", 1));
        addSkill(new Skill("探索", "图书馆使用", 20));
        addSkill(new Skill("探索", "导航", 10));
        addSkill(new Skill("技术", "计算机使用Ω", 5));
        addSkill(new Skill("社交", "取悦", 15));
        addSkill(new Skill("社交", "话术", 5));
        addSkill(new Skill("社交", "恐吓", 15));
        addSkill(new Skill("社交", "说服", 10));
        addSkill(new Skill("社交", "心理学", 10));
        addSkill(new Skill("语言", "母语", 0));
        addSkill(new Skill("语言", "外语", 1));
        addSkill(new Skill("战斗", "格斗(斗殴)", 25));
        addSkill(new Skill("战斗", "射击(手枪)", 20));
        addSkill(new Skill("战斗", "射击(步/霰)", 25));
        addSkill(new Skill("战斗", "闪避", 0));
        addSkill(new Skill("战斗", "投掷", 20));
        addSkill(new Skill("战斗", "爆破", 1));
        addSkill(new Skill("战斗", "炮术", 1));
        addSkill(new Skill("医疗", "急救", 30));
        addSkill(new Skill("医疗", "医学", 1));
        addSkill(new Skill("医疗", "精神分析", 1));
        addSkill(new Skill("医疗", "催眠", 1));
        addSkill(new Skill("运动", "攀爬", 20));
        addSkill(new Skill("运动", "跳跃", 20));
        addSkill(new Skill("运动", "游泳", 20));
        addSkill(new Skill("运动", "潜水", 1));
        addSkill(new Skill("知识", "估价", 5));
        addSkill(new Skill("知识", "人类学", 1));
        addSkill(new Skill("知识", "会计", 5));
        addSkill(new Skill("知识", "法律", 5));
        addSkill(new Skill("知识", "历史", 5));
        addSkill(new Skill("知识", "考古学", 1));
        addSkill(new Skill("知识", "博物学", 10));
        addSkill(new Skill("知识", "神秘学", 5));
        addSkill(new Skill("技术", "电子学Ω", 1));
        addSkill(new Skill("技术", "科学", 1));
        addSkill(new Skill("其它", "乔装", 5));
        addSkill(new Skill("其它", "生存", 5));
        addSkill(new Skill("其它", "技艺", 5));
        addSkill(new Skill("其它", "妙手", 10));
        addSkill(new Skill("其它", "锁匠", 1));
        addSkill(new Skill("技术", "电气维修", 10));
        addSkill(new Skill("技术", "机械维修", 10));
        addSkill(new Skill("操纵", "驯兽", 5));
        addSkill(new Skill("操纵", "骑术", 5));
        addSkill(new Skill("操纵", "操作重型机械", 1));
        addSkill(new Skill("操纵", "汽车驾驶", 20));
        addSkill(new Skill("操纵", "驾驶", 1));
    }

    // 添加技能
    public void addSkill(Skill skill) {
        skills.put(skill.getName(), skill);
    }

    // 获取技能
    public Skill getSkill(String skillName) {
        Skill skill = skills.get(skillName);
        if (skill == null) {
            throw new IllegalArgumentException("角色卡中不存在技能：" + skillName);
        }
        return skill;
    }

    // 设置技能成功率，如果技能不存在则创建一个自定义技能
    public void setSkill(String skillName, int successRate) {
        Skill skill = skills.get(skillName);
        if (skill == null) {
            skill = new Skill("自定义", skillName, 0);
            addSkill(skill);
        }
        skill.setSuccessRate(successRate);
    }

    // 获取所有技能
    public Map<String, Skill> getSkills() {
        return skills;
    }

    // 职业点
    public int getOccupationPoints() {
        return occupationPoints;
    }

    // 设置职业点
    public void setOccupationPoints(int occupationPoints) {
        CardValueValidator.checkNonNegative(occupationPoints, "职业点数");
        this.occupationPoints = occupationPoints;
    }

    // 剩余职业点
    public int getRemainingOccupationPoints() {
        return remainingOccupationPoints;
    }

    // 设置剩余职业点
    public void setRemainingOccupationPoints(int remainingOccupationPoints) {
        CardValueValidator.checkNonNegative(remainingOccupationPoints, "剩余职业点数");
        this.remainingOccupationPoints = remainingOccupationPoints;
    }

    // 兴趣点
    public int getInterestPoints() {
        return interestPoints;
    }

    // 设置兴趣点
    public void setInterestPoints(int interestPoints) {
        CardValueValidator.checkNonNegative(interestPoints, "兴趣点数");
        this.interestPoints = interestPoints;
    }

    // 剩余兴趣点
    public int getRemainingInterestPoints() {
        return remainingInterestPoints;
    }

    // 设置剩余兴趣点
    public void setRemainingInterestPoints(int remainingInterestPoints) {
        CardValueValidator.checkNonNegative(remainingInterestPoints, "剩余兴趣点数");
        this.remainingInterestPoints = remainingInterestPoints;
    }

    // 职业技能上限
    public int getOccupationSkillLimit() {
        return occupationSkillLimit;
    }

    // 设置职业技能上限
    public void setOccupationSkillLimit(int occupationSkillLimit) {
        CardValueValidator.checkPercentValue(occupationSkillLimit);
        this.occupationSkillLimit = occupationSkillLimit;
    }

    // 其他技能上限
    public int getOtherSkillLimit() {
        return otherSkillLimit;
    }

    // 设置其他技能上限
    public void setOtherSkillLimit(int otherSkillLimit) {
        CardValueValidator.checkPercentValue(otherSkillLimit);
        this.otherSkillLimit = otherSkillLimit;
    }
}
