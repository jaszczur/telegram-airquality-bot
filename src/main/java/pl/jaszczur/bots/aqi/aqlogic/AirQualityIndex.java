package pl.jaszczur.bots.aqi.aqlogic;

public enum AirQualityIndex {
    VERY_GOOD,
    GOOD,
    MODERATE,
    SUFFICIENT,
    BAD,
    VERY_BAD;

    public String getUiIndicator() {
        return "enum.AirQualityIndex." + name();
    }
}
