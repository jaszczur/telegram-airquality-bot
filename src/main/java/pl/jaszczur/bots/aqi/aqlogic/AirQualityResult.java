package pl.jaszczur.bots.aqi.aqlogic;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AirQualityResult {
    private final Station station;
    private final Map<PartType, Double> values;

    AirQualityResult(Station station, Map<PartType, Double> values) {
        this.station = station;
        this.values = values;
    }

    public Station getStation() {
        return station;
    }

    public Set<PartType> getAvailableParticleTypes() {
        return values.keySet();
    }

    public Optional<Double> getValue(PartType partType) {
        return Optional.ofNullable(values.get(partType));
    }

    @Override
    public String toString() {
        return "AirQualityResult{" +
                "station=" + station +
                ", values=" + values +
                '}';
    }
}