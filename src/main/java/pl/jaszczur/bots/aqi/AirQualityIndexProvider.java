package pl.jaszczur.bots.aqi;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

import java.util.Map;

public class AirQualityIndexProvider {
    private Map<PartType, Map<AirQualityIndex, Range<Double>>> ranges = ImmutableMap.of(
            PartType.PM25, ImmutableMap.<AirQualityIndex, Range<Double>>builder()
                    .put(AirQualityIndex.VERY_GOOD, Range.lessThan(12.0))
                    .put(AirQualityIndex.GOOD, Range.closedOpen(12.0, 36.0))
                    .put(AirQualityIndex.MODERATE, Range.closedOpen(36.0, 60.0))
                    .put(AirQualityIndex.SUFFICIENT, Range.closedOpen(60.0, 84.0))
                    .put(AirQualityIndex.BAD, Range.closedOpen(84.0, 120.0))
                    .put(AirQualityIndex.VERY_BAD, Range.atLeast(120.0)).build(),

            PartType.PM10, ImmutableMap.<AirQualityIndex, Range<Double>>builder()
                    .put(AirQualityIndex.VERY_GOOD, Range.lessThan(20.0))
                    .put(AirQualityIndex.GOOD, Range.closedOpen(20.0, 60.0))
                    .put(AirQualityIndex.MODERATE, Range.closedOpen(60.0, 100.0))
                    .put(AirQualityIndex.SUFFICIENT, Range.closedOpen(100.0, 140.0))
                    .put(AirQualityIndex.BAD, Range.closedOpen(140.0, 200.0))
                    .put(AirQualityIndex.VERY_BAD, Range.atLeast(200.0)).build()
    );

    public AirQualityIndex get(PartType partType, double value) {
        return ranges.get(partType).entrySet().stream()
                .filter(e -> e.getValue().test(value))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Couldn't find index for given params"));
    }
}
