package backend.dice;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/*
    * 骰子掷骰器类，负责解析骰子术语并生成掷骰结果。
    * 主要功能包括：
        1. 解析骰子术语：支持 NdM、NdMkhN、NdMklN、NdMdhN、NdMdlN 等格式。
        2. 生成随机数：根据解析结果生成对应的随机数列表。
        3. 计算总结果：根据生成的随机数和取舍规则计算总结果。
*/
public class DiceRoller {
    private static final Pattern DICE_TOKEN_PATTERN =
            Pattern.compile("(\\d*)d(\\d+|%)(?:(kh|kl|dh|dl)(\\d+))?", Pattern.CASE_INSENSITIVE);

    private final SecureRandom random = new SecureRandom();

    public DiceTermResult rollToken(String token) {
        String normalizedToken = token.toLowerCase();
        Matcher matcher = DICE_TOKEN_PATTERN.matcher(normalizedToken);

        if (!matcher.matches()) {
            throw new DiceError("无法识别骰子：" + token);
        }

        String countText = matcher.group(1);
        String sidesText = matcher.group(2);
        String mode = matcher.group(3);
        String nText = matcher.group(4);

        int count = countText == null || countText.isBlank() ? 1 : Integer.parseInt(countText);
        int sides = "%".equals(sidesText) ? 100 : Integer.parseInt(sidesText);

        checkDiceLimit(count, sides);

        List<Integer> rolls = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            rolls.add(random.nextInt(sides) + 1);
        }

        List<Integer> selected = new ArrayList<>(rolls);
        String note = "";

        if (mode != null) {
            int n = Integer.parseInt(nText);
            selected = selectRolls(rolls, mode, n);
            note = buildSelectNote(mode, n);
        }

        int total = selected.stream().mapToInt(Integer::intValue).sum();
        return new DiceTermResult(normalizedToken, total, rolls, note);
    }

    public int d100() {
        return random.nextInt(100) + 1;
    }

    private void checkDiceLimit(int count, int sides) {
        if (count <= 0) {
            throw new DiceError("骰子数量必须大于 0。");
        }

        if (sides <= 1) {
            throw new DiceError("骰子面数必须大于 1。");
        }

        if (count > DiceConstants.MAX_DICE_COUNT) {
            throw new DiceError("一次最多投 " + DiceConstants.MAX_DICE_COUNT + " 个骰子。");
        }

        if (sides > DiceConstants.MAX_DICE_SIDES) {
            throw new DiceError("骰子面数最多 " + DiceConstants.MAX_DICE_SIDES + "。");
        }
    }

    private List<Integer> selectRolls(List<Integer> rolls, String mode, int n) {
        int count = rolls.size();

        if (n <= 0) {
            throw new DiceError("取舍数量必须大于 0。");
        }

        if (("kh".equals(mode) || "kl".equals(mode)) && n > count) {
            throw new DiceError("保留数量不能超过骰子数量。");
        }

        if (("dh".equals(mode) || "dl".equals(mode)) && n >= count) {
            throw new DiceError("丢弃数量必须小于骰子数量。");
        }

        List<Integer> ordered = new ArrayList<>(rolls);
        Collections.sort(ordered);

        if ("kh".equals(mode)) {
            return new ArrayList<>(ordered.subList(count - n, count));
        }

        if ("kl".equals(mode)) {
            return new ArrayList<>(ordered.subList(0, n));
        }

        if ("dh".equals(mode)) {
            return new ArrayList<>(ordered.subList(0, count - n));
        }

        if ("dl".equals(mode)) {
            return new ArrayList<>(ordered.subList(n, count));
        }

        throw new DiceError("不支持的取舍模式：" + mode);
    }

    private String buildSelectNote(String mode, int n) {
        if ("kh".equals(mode)) {
            return "取高 " + n;
        }
        if ("kl".equals(mode)) {
            return "取低 " + n;
        }
        if ("dh".equals(mode)) {
            return "去高 " + n;
        }
        if ("dl".equals(mode)) {
            return "去低 " + n;
        }
        return "";
    }
}
