package pl.jaszczur.bots.aqi.aqlogic;

public enum AirQualityIndex {
    VERY_GOOD("✅ b. dobry"),
    GOOD("✅ dobry"),
    MODERATE("⚠ umiarkowany"),
    SUFFICIENT("⚠ dostateczny"),
    BAD("\uD83C\uDD98 zły"),
    VERY_BAD("\uD83C\uDD98 tragiczny");

    private String uiIndicator;

    AirQualityIndex(String uiIndicator) {

        this.uiIndicator = uiIndicator;
    }

    public String getUiIndicator() {
        return uiIndicator;
    }
}
