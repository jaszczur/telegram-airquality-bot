package pl.jaszczur.bots.aqi.aqlogic;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

import java.util.Map;

public class AirQualityIndexProvider {
    private Map<PartType, Map<AirQualityIndex, Range<Double>>> ranges = ImmutableMap.<PartType, Map<AirQualityIndex, Range<Double>>>builder()

            .put(PartType.SO2, ImmutableMap.<AirQualityIndex, Range<Double>>builder()
                    .put(AirQualityIndex.VERY_GOOD, Range.lessThan(50.0))
                    .put(AirQualityIndex.GOOD, Range.closedOpen(50.0, 100.0))
                    .put(AirQualityIndex.MODERATE, Range.closedOpen(100.0, 200.0))
                    .put(AirQualityIndex.SUFFICIENT, Range.closedOpen(200.0, 350.0))
                    .put(AirQualityIndex.BAD, Range.closedOpen(350.0, 500.0))
                    .put(AirQualityIndex.VERY_BAD, Range.atLeast(500.0)).build())

            .put(PartType.NO2, ImmutableMap.<AirQualityIndex, Range<Double>>builder()
                    .put(AirQualityIndex.VERY_GOOD, Range.lessThan(40.0))
                    .put(AirQualityIndex.GOOD, Range.closedOpen(40.0, 100.0))
                    .put(AirQualityIndex.MODERATE, Range.closedOpen(100.0, 150.0))
                    .put(AirQualityIndex.SUFFICIENT, Range.closedOpen(150.0, 200.0))
                    .put(AirQualityIndex.BAD, Range.closedOpen(200.0, 400.0))
                    .put(AirQualityIndex.VERY_BAD, Range.atLeast(400.0)).build())

            .put(PartType.CO, ImmutableMap.<AirQualityIndex, Range<Double>>builder()
                    .put(AirQualityIndex.VERY_GOOD, Range.lessThan(2000.0))
                    .put(AirQualityIndex.GOOD, Range.closedOpen(2000.0, 6000.0))
                    .put(AirQualityIndex.MODERATE, Range.closedOpen(6000.0, 10000.0))
                    .put(AirQualityIndex.SUFFICIENT, Range.closedOpen(10000.0, 14000.0))
                    .put(AirQualityIndex.BAD, Range.closedOpen(14000.0, 20000.0))
                    .put(AirQualityIndex.VERY_BAD, Range.atLeast(20000.0)).build())

            .put(PartType.PM10, ImmutableMap.<AirQualityIndex, Range<Double>>builder()
                    .put(AirQualityIndex.VERY_GOOD, Range.lessThan(20.0))
                    .put(AirQualityIndex.GOOD, Range.closedOpen(20.0, 60.0))
                    .put(AirQualityIndex.MODERATE, Range.closedOpen(60.0, 100.0))
                    .put(AirQualityIndex.SUFFICIENT, Range.closedOpen(100.0, 140.0))
                    .put(AirQualityIndex.BAD, Range.closedOpen(140.0, 200.0))
                    .put(AirQualityIndex.VERY_BAD, Range.atLeast(200.0)).build())

            .put(PartType.PM25, ImmutableMap.<AirQualityIndex, Range<Double>>builder()
                    .put(AirQualityIndex.VERY_GOOD, Range.lessThan(12.0))
                    .put(AirQualityIndex.GOOD, Range.closedOpen(12.0, 36.0))
                    .put(AirQualityIndex.MODERATE, Range.closedOpen(36.0, 60.0))
                    .put(AirQualityIndex.SUFFICIENT, Range.closedOpen(60.0, 84.0))
                    .put(AirQualityIndex.BAD, Range.closedOpen(84.0, 120.0))
                    .put(AirQualityIndex.VERY_BAD, Range.atLeast(120.0)).build())

            .put(PartType.O3, ImmutableMap.<AirQualityIndex, Range<Double>>builder()
                    .put(AirQualityIndex.VERY_GOOD, Range.lessThan(24.0))
                    .put(AirQualityIndex.GOOD, Range.closedOpen(24.0, 70.0))
                    .put(AirQualityIndex.MODERATE, Range.closedOpen(70.0, 120.0))
                    .put(AirQualityIndex.SUFFICIENT, Range.closedOpen(120.0, 160.0))
                    .put(AirQualityIndex.BAD, Range.closedOpen(160.0, 240.0))
                    .put(AirQualityIndex.VERY_BAD, Range.atLeast(240.0)).build())

            .put(PartType.C6H6, ImmutableMap.<AirQualityIndex, Range<Double>>builder()
                    .put(AirQualityIndex.VERY_GOOD, Range.lessThan(5.0))
                    .put(AirQualityIndex.GOOD, Range.closedOpen(5.0, 10.0))
                    .put(AirQualityIndex.MODERATE, Range.closedOpen(10.0, 15.0))
                    .put(AirQualityIndex.SUFFICIENT, Range.closedOpen(15.0, 20.0))
                    .put(AirQualityIndex.BAD, Range.closedOpen(20.0, 50.0))
                    .put(AirQualityIndex.VERY_BAD, Range.atLeast(50.0)).build())

            .build();

    public AirQualityIndex get(PartType partType, double value) {
        return ranges.get(partType).entrySet().stream()
                .filter(e -> e.getValue().test(value))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Couldn't find index for given params"));
    }
}
