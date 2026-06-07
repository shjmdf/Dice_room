package backend.dice;
/*
 * 安全的算术表达式求值器，支持 +、-、* 和括号。
*/
public class SafeArithmeticEvaluator {
    private String expression;
    private int index;

    public int evaluate(String expression) {
        this.expression = expression;
        this.index = 0;

        int result = parseExpression();

        if (index != expression.length()) {
            throw new DiceError("表达式里有非法字符。");
        }

        return result;
    }

    private int parseExpression() {
        int value = parseTerm();

        while (index < expression.length()) {
            char op = expression.charAt(index);

            if (op != '+' && op != '-') {
                break;
            }

            index++;
            int right = parseTerm();

            if (op == '+') {
                value += right;
            } else {
                value -= right;
            }
        }

        return value;
    }

    private int parseTerm() {
        int value = parseFactor();

        while (index < expression.length() && expression.charAt(index) == '*') {
            index++;
            value *= parseFactor();
        }

        return value;
    }

    private int parseFactor() {
        if (index >= expression.length()) {
            throw new DiceError("表达式不完整。");
        }

        char current = expression.charAt(index);

        if (current == '+') {
            index++;
            return parseFactor();
        }

        if (current == '-') {
            index++;
            return -parseFactor();
        }

        if (current == '(') {
            index++;
            int value = parseExpression();

            if (index >= expression.length() || expression.charAt(index) != ')') {
                throw new DiceError("括号不匹配。");
            }

            index++;
            return value;
        }

        return parseNumber();
    }

    private int parseNumber() {
        int start = index;

        while (index < expression.length() && Character.isDigit(expression.charAt(index))) {
            index++;
        }

        if (start == index) {
            throw new DiceError("表达式不支持。只支持 +、-、* 和括号。");
        }

        return Integer.parseInt(expression.substring(start, index));
    }
}
