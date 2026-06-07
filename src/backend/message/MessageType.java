package backend.message;

public enum MessageType {
    TEXT("text"),
    DICE("dice"),
    SKILL_CHECK("skill_check"),
    SYSTEM("system");
    private final String displayName;
    MessageType(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
    public boolean isText() {
        return this == TEXT;
    }
    public boolean isDice() {
        return this == DICE;
    }
    public boolean isSkillCheck() {
        return this == SKILL_CHECK;
    }
    public boolean isSystem() {
        return this == SYSTEM;
    }

}
