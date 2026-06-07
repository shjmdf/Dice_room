package backend.dice;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiceExpressionEvaluator {
    private static final Pattern DICE_PATTERN = Pattern.compile(
            "(?<![a-zA-Z0-9_])(\\d*d(?:\\d+|%)(?:(?:kh|kl|dh|dl)\\d+)?)(?![a-zA-Z0-9_])",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern REPEAT_PATTERN = Pattern.compile("(\\d+)#(.+)");
    private static final Pattern SAFE_EXPR_PATTERN = Pattern.compile("[0-9+\\-*()]+");

    private final DiceRoller roller;
    private final SafeArithmeticEvaluator arithmeticEvaluator;

    public DiceExpressionEvaluator() {
        this.roller = new DiceRoller();
        this.arithmeticEvaluator = new SafeArithmeticEvaluator();
    }

    public DiceExpressionResult evaluate(String rawExpression, boolean requireDice) {
        String expression = normalizeExpression(rawExpression);

        if (expression.length() > DiceConstants.MAX_EXPR_LEN) {
            throw new DiceError("表达式太长。");
        }

        List<DiceTermResult> diceTerms = new ArrayList<>();
        Matcher matcher = DICE_PATTERN.matcher(expression);
        StringBuffer replaced = new StringBuffer();

        while (matcher.find()) {
            if (diceTerms.size() >= DiceConstants.MAX_DICE_TERMS) {
                throw new DiceError("一个表达式最多包含 " + DiceConstants.MAX_DICE_TERMS + " 个骰子项。");
            }

            String token = matcher.group(1);
            DiceTermResult termResult = roller.rollToken(token);
            diceTerms.add(termResult);
            matcher.appendReplacement(replaced, String.valueOf(termResult.getTotal()));
        }

        matcher.appendTail(replaced);

        if (requireDice && diceTerms.isEmpty()) {
            throw new DiceError("没有找到骰子。例子：.r 1d100 或 .r 2d6+3");
        }

        String replacedExpression = replaced.toString();
        if (!SAFE_EXPR_PATTERN.matcher(replacedExpression).matches()) {
            throw new DiceError("表达式里有非法字符。只支持骰子、数字、+、-、*、括号。");
        }

        int finalResult = arithmeticEvaluator.evaluate(replacedExpression);
        return new DiceExpressionResult(expression, finalResult, diceTerms);
    }

    public RepeatRollResult rollRepeated(String rawExpression) {
        RepeatExpression repeatExpression = parseRepeat(rawExpression);
        List<DiceExpressionResult> results = new ArrayList<>();

        for (int i = 0; i < repeatExpression.repeat; i++) {
            results.add(evaluate(repeatExpression.expression, true));
        }

        return new RepeatRollResult(repeatExpression.repeat, repeatExpression.expression, results);
    }

    public String buildAdvantageExpression(String args, boolean high) {
        String token = high ? "2d20kh1" : "2d20kl1";

        if (args == null || args.isBlank()) {
            return token;
        }

        String expression = normalizeExpression(args);

        if (expression.startsWith("+") || expression.startsWith("-") || expression.startsWith("*")) {
            return token + expression;
        }

        if (expression.matches("\\d+")) {
            return token + "+" + expression;
        }

        Pattern d20Pattern = Pattern.compile("(?<![a-zA-Z0-9_])(?:1?d20)(?![a-zA-Z0-9_])",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = d20Pattern.matcher(expression);

        if (matcher.find()) {
            return matcher.replaceFirst(token);
        }

        throw new DiceError("优势/劣势只针对 d20。例子：.adv、.adv +5、.adv d20+5、.dis d20+5");
    }

    public SanCheckResult sanCheck(String args) {
        String[] parts = args == null ? new String[0] : args.trim().split("\\s+");

        if (parts.length < 2) {
            throw new DiceError("SC 格式：.sc 成功损失/失败损失 目标值，例如：.sc 1/1d6 60");
        }

        String lossPart = parts[0];
        String targetText = parts[1];

        if (!lossPart.contains("/")) {
            throw new DiceError("理智损失需要写成 成功/失败，例如：1/1d6");
        }

        if (!targetText.matches("(?:[1-9][0-9]?|100)")) {
            throw new DiceError("目标值必须是 1 到 100 的整数。例子：.sc 1/1d6 60");
        }

        String[] losses = lossPart.split("/", 2);
        int target = Integer.parseInt(targetText);
        int roll = roller.d100();
        boolean success = roll <= target;
        String usedLossExpression = success ? losses[0] : losses[1];
        DiceExpressionResult lossResult = evaluate(usedLossExpression, false);

        return new SanCheckResult(lossPart, target, roll, success, lossResult);
    }

    public String normalizeExpression(String rawExpression) {
        String expression = rawExpression == null ? "" : rawExpression.trim().toLowerCase();

        expression = expression
                .replace('＋', '+')
                .replace('－', '-')
                .replace('−', '-')
                .replace('—', '-')
                .replace('＊', '*')
                .replace('×', '*')
                .replace('（', '(')
                .replace('）', ')')
                .replace('％', '%')
                .replace('＃', '#');

        expression = expression.replaceAll("\\s+", "");
        return expression.isBlank() ? "1d100" : expression;
    }

    private RepeatExpression parseRepeat(String rawExpression) {
        String expression = normalizeExpression(rawExpression);
        Matcher matcher = REPEAT_PATTERN.matcher(expression);

        if (!matcher.matches()) {
            return new RepeatExpression(1, expression);
        }

        int repeat = Integer.parseInt(matcher.group(1));
        String innerExpression = matcher.group(2);

        if (repeat <= 0) {
            throw new DiceError("连续投掷次数必须大于 0。");
        }

        if (repeat > DiceConstants.MAX_REPEAT) {
            throw new DiceError("一次最多连续投 " + DiceConstants.MAX_REPEAT + " 次。");
        }

        return new RepeatExpression(repeat, innerExpression);
    }

    private static class RepeatExpression {
        private final int repeat;
        private final String expression;

        private RepeatExpression(int repeat, String expression) {
            this.repeat = repeat;
            this.expression = expression;
        }
    }
}
