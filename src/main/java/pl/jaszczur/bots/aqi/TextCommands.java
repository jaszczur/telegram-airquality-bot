package pl.jaszczur.bots.aqi;

import com.google.common.collect.ImmutableMap;

import java.util.Locale;
import java.util.Map;

public class TextCommands {
    private static Map<Locale, Map<String, String>> messages = ImmutableMap.of(
            Locale.forLanguageTag("pl_PL"), ImmutableMap.<String, String>builder()
                    .put("cmd.refresh", "\uD83D\uDD01 Podaj aktualne wartości")
                    .put("cmd.set_station", "\uD83D\uDD00 Zmień stację")
                    .put("msg.hello", "Siema. Najpierw proponuję ustawić swoją lokalizację.")
                    .build());

    public static String getText(Locale locale, String msgId) {
        return messages.get(locale).get(msgId);
    }
}
