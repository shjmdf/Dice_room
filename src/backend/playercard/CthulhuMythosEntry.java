package backend.playercard;

/*
 * 单条克苏鲁神话记录。
 */
public class CthulhuMythosEntry {
    private String entryType;
    private String name;
    private String description;
    private String source;

    public CthulhuMythosEntry(String entryType, String name, String description, String source) {
        this.entryType = entryType;
        this.name = name;
        this.description = description;
        this.source = source;
    }

    public String getEntryType() {
        return entryType;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSource() {
        return source;
    }
}
