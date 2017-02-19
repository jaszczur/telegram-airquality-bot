package pl.jaszczur.bots.aqi;

import com.google.common.base.Strings;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.response.BaseResponse;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.jaszczur.bots.aqi.aqlogic.AirQualityApi;
import pl.jaszczur.bots.aqi.aqlogic.AirQualityIndexProvider;
import pl.jaszczur.bots.aqi.commands.GetAirQualityCommand;
import pl.jaszczur.bots.aqi.commands.SetLocationCommand;
import pl.jaszczur.bots.aqi.commands.StartCommand;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private final TelegramBot bot;
    private final BotHandler botHandler;
    private ChatStates chatStates = new ChatStates();

    public Main(TelegramBot bot) {
        this.bot = bot;
        this.botHandler = new BotHandler(chatStates);
    }

    public void start() {
        AirQualityApi airQualityApi = new AirQualityApi();

        StartCommand startCommand = new StartCommand(chatStates);
        SetLocationCommand setLocationCommand = new SetLocationCommand(chatStates, airQualityApi);
        GetAirQualityCommand getAirQualityCommand = new GetAirQualityCommand(airQualityApi, new AirQualityIndexProvider(), chatStates);

        botHandler
                .addCommand(startCommand)
                .addCommand(setLocationCommand)
                .addCommand(getAirQualityCommand);

        bot.setUpdatesListener(updates -> {
            Flowable.fromIterable(updates)
                    .map(Update::message)
                    .flatMap(message -> botHandler.handle(message).toFlowable())
                    .map(bot::execute)
                    .subscribe(
                            msg -> {
                                if (msg.isOk())
                                    logger.debug("Reply sent");
                                else
                                    logger.warn("Error occurred {}: {}", msg.errorCode(), msg.description());
                            },
                            err -> logger.error("What a Terrible Failure", err));
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    public static void main(String[] args) {
        logger.info("Starting the bot");
        String botApiKey = args.length > 0 ? args[0] : System.getenv("TELEGRAM_BOT_KEY");
        if (Strings.isNullOrEmpty(botApiKey)) {
            logger.error("Specify TELEGRAM_BOT_KEY by defining env variable or providing an argument");
            System.exit(-1);
        } else {
            new Main(TelegramBotAdapter.build(botApiKey)).start();
        }
    }

}
