package pl.jaszczur.bots.aqi;

public enum AirQualityIndex {
    VERY_GOOD("✅ b. dobry"),
    GOOD("✅ dobry"),
    MODERATE("⚠ umiarkowany"),
    SUFFICIENT("⚠ dostateczny"),
    BAD("\uDD98 zły"),
    VERY_BAD("\uDD98 tragiczny");

    private String uiIndicator;

    AirQualityIndex(String uiIndicator) {

        this.uiIndicator = uiIndicator;
    }

    public String getUiIndicator() {
        return uiIndicator;
    }
}
