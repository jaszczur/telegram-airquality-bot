package pl.jaszczur.bots.aqi;

import com.google.common.collect.ImmutableMap;

import java.util.Locale;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class TextCommands {
    private static Map<Locale, Map<String, String>> messages = ImmutableMap.of(
            Locale.forLanguageTag("pl_PL"), ImmutableMap.<String, String>builder()
                    .put("cmd.refresh", "\uD83D\uDD01 Podaj aktualne wartości")
                    .put("cmd.set_station", "\uD83D\uDD00 Zmień stację")
                    .put("msg.hello", "Siema. Najpierw proponuję ustawić swoją lokalizację.")
                    .put("enum.AirQualityIndex.VERY_GOOD", "\uD83C\uDF40 wyśmienity")
                    .put("enum.AirQualityIndex.GOOD", "✅ dobry")
                    .put("enum.AirQualityIndex.MODERATE", "⚠️ umiarkowany")
                    .put("enum.AirQualityIndex.SUFFICIENT", "\uD83D\uDEA8 dostateczny")
                    .put("enum.AirQualityIndex.BAD", "\uD83C\uDD98 zły")
                    .put("enum.AirQualityIndex.VERY_BAD", "☠ tragiczny")
                    .build());

    public static String getText(Locale locale, String msgId) {
        return checkNotNull(messages.get(locale).get(msgId), "translation not found: " + msgId);
    }
}
