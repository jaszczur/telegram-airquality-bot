package pl.jaszczur.bots.aqi;

import com.google.common.collect.Ordering;
import io.reactivex.Single;
import pl.jaszczur.bots.aqi.aqlogic.AirQualityApi;
import pl.jaszczur.bots.aqi.aqlogic.AirQualityIndexProvider;
import pl.jaszczur.bots.aqi.aqlogic.AirQualityResult;
import pl.jaszczur.bots.aqi.aqlogic.PartType;

import java.util.List;
import java.util.Locale;

public class AirQualityMessageProvider {
    private final AirQualityApi airQualityApi;
    private final AirQualityIndexProvider airQualityIndexProvider;

    public AirQualityMessageProvider(AirQualityApi airQualityApi, AirQualityIndexProvider airQualityIndexProvider) {
        this.airQualityApi = airQualityApi;
        this.airQualityIndexProvider = airQualityIndexProvider;
    }

    public Single<String> getMessage(Locale locale, long stationId) {
        return airQualityApi.getStats(stationId).map(aqr -> formatMessage(locale, aqr));
    }

    private String formatMessage(Locale locale, AirQualityResult airQualityResult) {
        StringBuilder result = new StringBuilder();
        result.append("Aktualne poziomy dla stacji *").append(airQualityResult.getStation().getName()).append("*:\n");
        result.append("\n");
        for (PartType partType : sortedParticles(airQualityResult)) {
            double value = airQualityResult.getValue(partType);
            result.append("- ")
                    .append(partType.getUiName())
                    .append(": *")
                    .append(String.format(locale, "%.1f", value))
                    .append(" µg/m³* ")
                    .append(TextCommands.getText(locale, airQualityIndexProvider.get(partType, value).getUiIndicator()))
                    .append("\n");
        }
        return result.toString();
    }

    private List<PartType> sortedParticles(AirQualityResult airQualityResult) {
        return Ordering.usingToString().sortedCopy(airQualityResult.getAvailableParticleTypes());
    }
}
