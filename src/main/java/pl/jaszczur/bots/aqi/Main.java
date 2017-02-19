package pl.jaszczur.bots.aqi;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import io.reactivex.Flowable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class Main {
    private final TelegramBot bot;
    private final BotHandler botHandler;
    private ChatStates chatStates = new ChatStates();

    public Main(TelegramBot bot) {
        this.bot = bot;
        this.botHandler = new BotHandler(chatStates);
    }

    public void start() {
        StartCommand startCommand = new StartCommand(chatStates);
        SetLocationCommand setLocationCommand = new SetLocationCommand(chatStates);
        GetAirQualityCommand getAirQualityCommand = new GetAirQualityCommand(new AirQualityApi(), chatStates);

        botHandler
                .addCommand(startCommand)
                .addCommand(setLocationCommand)
                .addCommand(getAirQualityCommand);

        bot.setUpdatesListener(updates -> {
            Flowable.fromIterable(updates)
                    .map(Update::message)
                    .flatMap(message -> botHandler.handle(message).toFlowable())
                    .map(bot::execute)
                    .map(msg -> {
                        if (msg.isOk())
                            return msg;
                        else throw new MessageDeliveryException(msg);
                    })
                    .subscribe(
                            msg -> System.out.println("Reply sent"),
                            Throwable::printStackTrace);

            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    public static void main(String[] args) {
        System.out.println("Odpytujemy serwera");
        new Main(TelegramBotAdapter.build(args[0])).start();
    }

}
