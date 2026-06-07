package backend.playercard;

import backend.playercard.sheet.PlayerCardSheet;
import backend.playercard.sheet.PlayerCardSheetMapper;

public class PlayerCardSheetTemplate {
    private PlayerCardSheetTemplate() {
    }

    public static String createDefaultSheet(String name) {
        return createSheet(new PlayerCard(0, 0, name));
    }

    public static PlayerCardSheet createDefaultSheetObject(String name) {
        return createSheetObject(new PlayerCard(0, 0, name));
    }

    public static String createSheet(PlayerCard card) {
        return PlayerCardSheetMapper.toJson(createSheetObject(card));
    }

    public static PlayerCardSheet createSheetObject(PlayerCard card) {
        if (card == null) {
            throw new IllegalArgumentException("角色卡不能为空");
        }
        return PlayerCardSheetMapper.fromPlayerCard(card);
    }
}
