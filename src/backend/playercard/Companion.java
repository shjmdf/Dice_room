package backend.playercard;

/*
 * 伙伴信息。
 *
 * 包含伙伴名称、关系和玩家名称等字段。
 */
public class Companion {
    private String characterName;
    private String relationship;
    private String playerName;

    public Companion(String characterName, String relationship, String playerName) {
        this.characterName = characterName;
        this.relationship = relationship;
        this.playerName = playerName;
    }

    public String getCharacterName() {
        return characterName;
    }

    public String getRelationship() {
        return relationship;
    }

    public String getPlayerName() {
        return playerName;
    }
}
