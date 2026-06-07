package backend.dice;

import java.util.ArrayList;
import java.util.List;
/*
    * 骰子表达式结果类，表示一次骰子表达式的结果。
    * 包含以下字段：
        - expression：掷骰的原始表达式文本
        - finalResult：掷骰的最终结果
        - diceTerms：掷骰过程中的结果列表
 */
public class DiceExpressionResult {
    private final String expression;
    private final int finalResult;
    private final List<DiceTermResult> diceTerms;

    public DiceExpressionResult(String expression, int finalResult, List<DiceTermResult> diceTerms) {
        this.expression = expression;
        this.finalResult = finalResult;
        this.diceTerms = new ArrayList<>(diceTerms);
    }

    public String getExpression() {
        return expression;
    }

    public int getFinalResult() {
        return finalResult;
    }

    public List<DiceTermResult> getDiceTerms() {
        return new ArrayList<>(diceTerms);
    }

    public List<String> getDetails() {
        List<String> details = new ArrayList<>();

        for (DiceTermResult term : diceTerms) {
            details.add(term.formatDetail());
        }

        return details;
    }
}
