package backend.playercard;

/*
 * 角色特征值。
 * 继承了Characteristic类，包含特征名称、代码、掷骰公式和数值等字段。
 * 此处是角色的基本属性
 * 
*/
public class Characteristics {
    private Characteristic str;
    private Characteristic dex;
    private Characteristic siz;
    private Characteristic app;
    private Characteristic con;
    private Characteristic intel;
    private Characteristic pow;
    private Characteristic edu;
    private Characteristic luck;
    private ResourceValue hp;
    private ResourceValue mp;
    private ResourceValue san;

    public Characteristics() {
        this.str = new Characteristic("力量", "STR", "3d6*5");
        this.dex = new Characteristic("敏捷", "DEX", "3d6*5");
        this.siz = new Characteristic("体型", "SIZ", "(2d6+6)*5");
        this.app = new Characteristic("外貌", "APP", "3d6*5");
        this.con = new Characteristic("体质", "CON", "3d6*5");
        this.intel = new Characteristic("智力", "INT", "(2d6+6)*5");
        this.pow = new Characteristic("意志", "POW", "3d6*5");
        this.edu = new Characteristic("教育", "EDU", "(2d6+6)*5");
        this.luck = new Characteristic("幸运", "LUC", "3d6*5");
        this.hp = new ResourceValue(0, 0, 0);
        this.mp = new ResourceValue(0, 0, 0);
        this.san = new ResourceValue(0, 0, 99);
    }

    public Characteristic getStr() {
        return str;
    }

    public Characteristic getDex() {
        return dex;
    }

    public Characteristic getSiz() {
        return siz;
    }

    public Characteristic getApp() {
        return app;
    }

    public Characteristic getCon() {
        return con;
    }

    public Characteristic getIntel() {
        return intel;
    }

    public Characteristic getPow() {
        return pow;
    }

    public Characteristic getEdu() {
        return edu;
    }

    public Characteristic getLuck() {
        return luck;
    }

    public ResourceValue getHp() {
        return hp;
    }

    public ResourceValue getMp() {
        return mp;
    }

    public ResourceValue getSan() {
        return san;
    }
}
