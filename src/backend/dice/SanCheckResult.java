package backend.dice;

public class SanCheckResult {
    private final String lossExpression;
    private final int target;
    private final int roll;
    private final boolean success;
    private final DiceExpressionResult lossResult;

    public SanCheckResult(String lossExpression, int target, int roll, boolean success,
            DiceExpressionResult lossResult) {
        this.lossExpression = lossExpression;
        this.target = target;
        this.roll = roll;
        this.success = success;
        this.lossResult = lossResult;
    }

    public String getLossExpression() {
        return lossExpression;
    }

    public int getTarget() {
        return target;
    }

    public int getRoll() {
        return roll;
    }

    public boolean isSuccess() {
        return success;
    }

    public DiceExpressionResult getLossResult() {
        return lossResult;
    }
}
