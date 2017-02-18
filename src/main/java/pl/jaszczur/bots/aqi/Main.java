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
        AirQualityResult airQualityResult = airQualityApi.getStats(117).blockingGet();
        System.out.println(airQualityResult);

        final Subject<Update> updatesSubject = PublishSubject.create();
        final TelegramBot bot = TelegramBotAdapter.build(args[0]);

        updatesSubject.forEach(update -> {
            long stationId = Long.parseLong(update.message().text());
            airQualityApi.getStats(stationId).subscribe(aqi -> {
                SendMessage request = new SendMessage(update.message().chat().id(), aqi.toString())
                        .parseMode(ParseMode.Markdown)
                        .disableWebPagePreview(true)
                        .disableNotification(true)
                        .replyToMessageId(update.message().messageId());
                bot.execute(request);
            });
        });

        bot.setUpdatesListener(updates -> {
            updates.forEach(updatesSubject::onNext);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

}
