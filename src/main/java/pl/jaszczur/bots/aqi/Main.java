package pl.jaszczur.bots.aqi;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class Main {
    private final TelegramBot bot;
    private final BotHandler botHandler;
    private BotState botState = new BotState();

    public Main(TelegramBot bot) {
        this.bot = bot;
        this.botHandler = new BotHandler(bot);
    }

    public void start() {
        botHandler
                .addCommand(new StartCommand())
                .addCommand(new SetLocationCommand(botState))
                .addCommand(new GetAirQualityCommand(new AirQualityApi(), botState));

        Keyboard keyboard = new ReplyKeyboardMarkup(
                new KeyboardButton[]{
                        new KeyboardButton("/set_station").requestLocation(true),
                        new KeyboardButton("/get").requestContact(true)
                }
        );

        final Subject<Update> updatesSubject = PublishSubject.create();
        updatesSubject.forEach(update -> {
            Message message = update.message();
            botHandler.handle(message);
        });
        bot.setUpdatesListener(updates -> {
            updates.forEach(updatesSubject::onNext);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    public static void main(String[] args) {
        System.out.println("Odpytujemy serwera");
        new Main(TelegramBotAdapter.build(args[0])).start();
    }

}
