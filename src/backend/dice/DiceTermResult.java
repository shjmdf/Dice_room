package backend.dice;

import java.util.ArrayList;
import java.util.List;

/*
    * 骰子结果类，表示一次骰子术语的结果。
    * 包含以下字段：
        - token：掷骰的原始文本
        - total：掷骰的总结果
        - rolls：掷骰的具体结果列表
        - note：掷骰的备注信息（如果有）
 */
public class DiceTermResult {
    private final String token;
    private final int total;
    private final List<Integer> rolls;
    private final String note;

    public DiceTermResult(String token, int total, List<Integer> rolls, String note) {
        this.token = token;
        this.total = total;
        this.rolls = new ArrayList<>(rolls);
        this.note = note;
    }

    public String getToken() {
        return token;
    }

    public int getTotal() {
        return total;
    }

    public List<Integer> getRolls() {
        return new ArrayList<>(rolls);
    }

    public String getNote() {
        return note;
    }

    public String formatDetail() {
        String shownRolls = formatRolls();

        if (note != null && !note.isBlank()) {
            return token + " = " + shownRolls + "，" + note + " = " + total;
        }

        return token + " = " + shownRolls + " = " + total;
    }

    private String formatRolls() {
        if (rolls.size() <= DiceConstants.MAX_SHOW_ROLLS) {
            return rolls.toString();
        }

        List<Integer> shown = rolls.subList(0, DiceConstants.MAX_SHOW_ROLLS);
        return shown + " ... 共 " + rolls.size() + " 个";
    }
}
