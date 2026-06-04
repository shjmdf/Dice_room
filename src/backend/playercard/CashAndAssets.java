package backend.playercard;

/*
 * 角色卡的现金与资产信息。
 *
 * 包含信用评级、现金、消费水平和资产等字段。
*/
public class CashAndAssets {
    // 信用评级
    private int creditRating;
    // 现金
    private String cash;
    // 消费水平
    private String spendingLevel;
    // 资产
    private String assets;

    public CashAndAssets() {
        this.creditRating = 0;
        this.cash = "";
        this.spendingLevel = "";
        this.assets = "";
    }

    public int getCreditRating() {
        return creditRating;
    }

    public void setCreditRating(int creditRating) {
        CardValueValidator.checkPercentValue(creditRating);
        this.creditRating = creditRating;
    }

    public String getCash() {
        return cash;
    }

    public void setCash(String cash) {
        this.cash = cash;
    }

    public String getSpendingLevel() {
        return spendingLevel;
    }

    public void setSpendingLevel(String spendingLevel) {
        this.spendingLevel = spendingLevel;
    }

    public String getAssets() {
        return assets;
    }

    public void setAssets(String assets) {
        this.assets = assets;
    }
}
