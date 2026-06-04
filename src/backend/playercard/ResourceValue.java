package backend.playercard;

/*
 * 资源值。
 *
 * 包含现有值、初始值和最大值等字段。
 */
public class ResourceValue {
    private int current;
    private int initial;
    private int max;

    public ResourceValue(int current, int initial, int max) {
        setCurrent(current);
        setInitial(initial);
        setMax(max);
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        CardValueValidator.checkNonNegative(current, "现有值");
        this.current = current;
    }

    public int getInitial() {
        return initial;
    }

    public void setInitial(int initial) {
        CardValueValidator.checkNonNegative(initial, "初始值");
        this.initial = initial;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        CardValueValidator.checkNonNegative(max, "最大值");
        this.max = max;
    }
}
