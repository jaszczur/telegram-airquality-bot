package pl.jaszczur.bots.aqi;

import java.util.Map;

class AirQualityResult {
    private final Station station;
    private final Map<PartType, Double> values;

    AirQualityResult(Station station, Map<PartType, Double> values) {
        this.station = station;
        this.values = values;
    }

    public Station getStation() {
        return station;
    }

    public Map<PartType, Double> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return "AirQualityResult{" +
                "station=" + station +
                ", values=" + values +
                '}';
    }
}
