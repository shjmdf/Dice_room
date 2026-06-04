package backend.playercard;

import java.util.ArrayList;
import java.util.List;

/*
 * 克苏鲁神话信息。
 *
 * 一个调查员可能多次接触神话信息，所以这里保存多条神话条目。
 */
public class CthulhuMythos {
    private int mythosSkillValue;
    private List<CthulhuMythosEntry> entries;

    public CthulhuMythos() {
        this.mythosSkillValue = 0;
        this.entries = new ArrayList<>();
    }

    public int getMythosSkillValue() {
        return mythosSkillValue;
    }

    public void setMythosSkillValue(int mythosSkillValue) {
        CardValueValidator.checkPercentValue(mythosSkillValue);
        this.mythosSkillValue = mythosSkillValue;
    }

    public List<CthulhuMythosEntry> getEntries() {
        return entries;
    }

    public void addEntry(CthulhuMythosEntry entry) {
        entries.add(entry);
    }
}
