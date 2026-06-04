package backend.playercard;

/*
 * 单条经历模组记录。
 */
public class ExperiencedScenarioEntry {
    private String scenarioName;
    private String experience;

    public ExperiencedScenarioEntry(String scenarioName, String experience) {
        this.scenarioName = scenarioName;
        this.experience = experience;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public String getExperience() {
        return experience;
    }
}
