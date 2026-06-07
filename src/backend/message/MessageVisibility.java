package backend.message;

public enum MessageVisibility {
    PUBLIC("Public"),
    PRIVATE("Private");
    private final String displayName;
    MessageVisibility(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
    public boolean isPublic() {
        return this == PUBLIC;
    }
    public boolean isPrivate() {
        return this == PRIVATE;
    }
}
