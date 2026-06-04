package backend.playercard;

/*
 * 角色卡数值验证器。
 *
 * 提供静态方法用于验证角色卡数值的合法性，如百分比数值必须在 0 到 100 之间，资源数值必须为非负整数等。
*/
public final class CardValueValidator {
    private CardValueValidator() {
    }

    public static void checkPercentValue(int value) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("数值必须在 0 到 100 之间");
        }
    }

    public static void checkNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " 不能小于 0");
        }
    }
}
