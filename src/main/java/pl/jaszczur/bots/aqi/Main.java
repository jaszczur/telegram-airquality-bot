package pl.jaszczur.bots.aqi;

import com.google.common.collect.Maps;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import java.util.Map;

public class Main {
    private final AirQualityApi airQualityApi = new AirQualityApi();
    private final TelegramBot bot;
    private final Map<Long, Long> stationIdByChatId = Maps.newConcurrentMap();

    public Main(TelegramBot bot) {
        this.bot = bot;
    }

    public void start() {

        final Subject<Update> updatesSubject = PublishSubject.create();

        updatesSubject.forEach(update -> {
            Chat chat = update.message().chat();
            try {
                long stationId = Long.parseLong(update.message().text());
                checkAirQuality(chat, stationId, aqi -> {
                    stationIdByChatId.put(chat.id(), stationId);
                    sendMessage(chat, formatMessage(aqi));
                });
            } catch (NumberFormatException ex) {
                Long stationId = stationIdByChatId.get(chat.id());
                if (stationId == null) {
                    sendMessage(chat, "Podaj numer stacji, np 117");
                } else {
                    checkAirQuality(chat, stationId, aqi -> {
                        sendMessage(chat, formatMessage(aqi));
                    });
                }
            }
        });

        bot.setUpdatesListener(updates -> {
            updates.forEach(updatesSubject::onNext);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void checkAirQuality(Chat chat, long stationId, Consumer<AirQualityResult> airQualityResultConsumer) {
        airQualityApi.getStats(stationId).subscribe(
                airQualityResultConsumer,
                err -> sendMessage(chat, "Coś nie bangla. Chyba podałeś/aś numer stacji z dupy..."));
    }

    private void sendMessage(Chat chat, String text) {
        SendMessage request = new SendMessage(chat.id(), text)
                .parseMode(ParseMode.Markdown)
                .disableWebPagePreview(true)
                .disableNotification(true);
        bot.execute(request);
    }

    private String formatMessage(AirQualityResult airQualityResult) {
        return airQualityResult.getStation().getName() + ":\n"
                + " - Pyłki PM2.5: *" + airQualityResult.getValues().get(PartType.PM25) + " µg/m³*\n"
                + " - Pyłki PM10: *" + airQualityResult.getValues().get(PartType.PM10) + " µg/m³*\n";
    }

    public static void main(String[] args) {
        System.out.println("Odpytujemy serwera");
        new Main(TelegramBotAdapter.build(args[0])).start();
    }

}
