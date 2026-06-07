package service;

import backend.dice.DiceExpressionEvaluator;
import backend.dice.DiceExpressionResult;
import backend.dice.DiceRoller;
import backend.dice.RepeatRollResult;
import backend.dice.SanCheckResult;
import backend.dice.SkillCheckResult;
import backend.playercard.sheet.PlayerCardSheetMapper.SkillValue;

/*
 * 掷骰业务服务。
 *
 * DiceExpressionEvaluator 负责纯表达式计算。
 * DiceService 负责把掷骰和角色卡业务串起来。
 */
public class DiceService {
    private final PlayerCardService playerCardService;
    private final DiceExpressionEvaluator expressionEvaluator;
    private final DiceRoller diceRoller;

    public DiceService(PlayerCardService playerCardService) {
        this.playerCardService = playerCardService;
        this.expressionEvaluator = new DiceExpressionEvaluator();
        this.diceRoller = new DiceRoller();
    }

    public DiceExpressionResult roll(String expression) {
        return expressionEvaluator.evaluate(expression, true);
    }

    public RepeatRollResult rollRepeated(String expression) {
        return expressionEvaluator.rollRepeated(expression);
    }

    public SanCheckResult sanCheck(String args) {
        return expressionEvaluator.sanCheck(args);
    }

    public SkillCheckResult checkSkill(int userId, int cardId, String skillName) {
        if (skillName == null || skillName.isBlank()) {
            throw new IllegalArgumentException("技能名称不能为空");
        }

        SkillValue skill = playerCardService.getSkillValue(userId, cardId, skillName);
        int roll = diceRoller.d100();
        String successLevel = judgeCocSuccess(roll, skill);

        return new SkillCheckResult(
                userId,
                cardId,
                skill.skillName(),
                skill.successRate(),
                skill.hardRate(),
                skill.extremeRate(),
                roll,
                successLevel);
    }

    private String judgeCocSuccess(int roll, SkillValue skill) {
        if (roll == 1) {
            return "大成功";
        }

        if (isFumble(roll, skill.successRate())) {
            return "大失败";
        }

        if (roll <= skill.extremeRate()) {
            return "极难成功";
        }

        if (roll <= skill.hardRate()) {
            return "困难成功";
        }

        if (roll <= skill.successRate()) {
            return "普通成功";
        }

        return "失败";
    }

    private boolean isFumble(int roll, int successRate) {
        if (successRate <= 50) {
            return roll >= 96;
        }

        return roll == 100;
    }
}
