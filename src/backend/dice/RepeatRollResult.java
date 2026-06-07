package backend.dice;

import java.util.ArrayList;
import java.util.List;
/*
    * 重复掷骰结果类，表示一次重复掷骰的结果。
    * 包含以下字段：
        - repeat：重复的次数
        - expression：掷骰的原始表达式文本
        - results：每次掷骰的结果列表
 */
public class RepeatRollResult {
    private final int repeat;
    private final String expression;
    private final List<DiceExpressionResult> results;

    public RepeatRollResult(int repeat, String expression, List<DiceExpressionResult> results) {
        this.repeat = repeat;
        this.expression = expression;
        this.results = new ArrayList<>(results);
    }

    public int getRepeat() {
        return repeat;
    }

    public String getExpression() {
        return expression;
    }

    public List<DiceExpressionResult> getResults() {
        return new ArrayList<>(results);
    }
}
