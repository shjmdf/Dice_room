package backend.playercard;

/* 
 * 角色特征值。
 *
 * 包含特征名称、代码、掷骰公式和数值等字段。
 */
public class Characteristic {
    private String name;
    private String code;
    private String rollFormula;
    private int value;
    private int hardValue;
    private int extremeValue;

    public Characteristic(String name, String code, String rollFormula) {
        this.name = name;
        this.code = code;
        this.rollFormula = rollFormula;
        setValue(0);
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getRollFormula() {
        return rollFormula;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        CardValueValidator.checkPercentValue(value);
        this.value = value;
        this.hardValue = Math.max(1, value / 2);
        this.extremeValue = Math.max(1, value / 5);
    }

    public int getHardValue() {
        return hardValue;
    }

    public int getExtremeValue() {
        return extremeValue;
    }
}
