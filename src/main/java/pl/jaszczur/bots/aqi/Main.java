package pl.jaszczur.bots.aqi;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class Main {
    private final TelegramBot bot;
    private final BotHandler botHandler;
    private ChatStates chatStates = new ChatStates();

    public Main(TelegramBot bot) {
        this.bot = bot;
        this.botHandler = new BotHandler(bot, chatStates);
    }

    public void start() {
        StartCommand startCommand = new StartCommand(chatStates);
        SetLocationCommand setLocationCommand = new SetLocationCommand(chatStates);
        GetAirQualityCommand getAirQualityCommand = new GetAirQualityCommand(new AirQualityApi(), chatStates);

        botHandler
                .addCommand(startCommand)
                .addCommand(setLocationCommand)
                .addCommand(getAirQualityCommand);

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
