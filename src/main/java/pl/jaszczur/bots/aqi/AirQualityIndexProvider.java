package pl.jaszczur.bots.aqi;

public class AirQualityIndexProvider {

    public AirQualityIndex get(PartType partType, double value) {
        return AirQualityIndex.GOOD;
    }
}
