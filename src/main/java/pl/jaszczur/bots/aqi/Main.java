package pl.jaszczur.bots.aqi;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class Main {
    public static void main(String[] args) {
        System.out.println("Odpytujemy serwera");
        AirQualityApi airQualityApi = new AirQualityApi();

        final Subject<Update> updatesSubject = PublishSubject.create();
        final TelegramBot bot = TelegramBotAdapter.build(args[0]);

        updatesSubject.forEach(update -> {
            try {
                long stationId = Long.parseLong(update.message().text());
                airQualityApi.getStats(stationId).subscribe(aqi -> {
                    SendMessage request = new SendMessage(update.message().chat().id(), formatMessage(aqi))
                            .parseMode(ParseMode.Markdown)
                            .disableWebPagePreview(true)
                            .disableNotification(true)
                            .replyToMessageId(update.message().messageId());
                    bot.execute(request);
                });
            } catch (NumberFormatException ex) {

                SendMessage request = new SendMessage(update.message().chat().id(), "Podaj numer stacji, np 117")
                        .parseMode(ParseMode.Markdown)
                        .disableWebPagePreview(true)
                        .disableNotification(true)
                        .replyToMessageId(update.message().messageId());
                bot.execute(request);
            }
        });

        bot.setUpdatesListener(updates -> {
            updates.forEach(updatesSubject::onNext);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private static String formatMessage(AirQualityResult airQualityResult) {
        return  airQualityResult.getStation().getName() + ":\n"
                + " - Pyłki PM2.5: *" + airQualityResult.getValues().get(PartType.PM25) + " µg/m3*\n"
                + " - Pyłki PM10: *" + airQualityResult.getValues().get(PartType.PM10) + " µg/m3*\n";
    }

}
