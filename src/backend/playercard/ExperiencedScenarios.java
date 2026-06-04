package backend.playercard;

import java.util.ArrayList;
import java.util.List;

/*
 * 经历过的模组集合。
 */
public class ExperiencedScenarios {
    private List<ExperiencedScenarioEntry> entries;

    public ExperiencedScenarios() {
        this.entries = new ArrayList<>();
    }

    public List<ExperiencedScenarioEntry> getEntries() {
        return entries;
    }

    public void addEntry(ExperiencedScenarioEntry entry) {
        entries.add(entry);
    }
}
