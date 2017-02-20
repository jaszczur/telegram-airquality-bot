package pl.jaszczur.bots.aqi;

import com.google.common.base.Strings;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.jaszczur.bots.aqi.aqlogic.AirQualityApi;
import pl.jaszczur.bots.aqi.aqlogic.AirQualityIndexProvider;
import pl.jaszczur.bots.aqi.commands.GetAirQualityCommand;
import pl.jaszczur.bots.aqi.commands.SetLocationCommand;
import pl.jaszczur.bots.aqi.commands.StartCommand;
import pl.jaszczur.bots.aqi.state.ChatStates;
import pl.jaszczur.bots.aqi.state.Storage;

import java.io.File;
import java.util.List;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String STATE_STORAGE_FILE_PATH = "./chat-state.json";
    private final TelegramBot bot;
    private final Storage storage;

    private Main(TelegramBot bot, Storage storage) {
        this.bot = bot;
        this.storage = storage;
    }

    private void start() {
        storage.load().subscribe(chatStates -> {
            final BotHandler botHandler = createBotHandler(chatStates);
            bot.setUpdatesListener(updates -> processUpdates(botHandler, updates));
        });
    }

    private int processUpdates(BotHandler botHandler, List<Update> updates) {
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

    }

    private BotHandler createBotHandler(ChatStates chatStates) {
        final AirQualityApi airQualityApi = new AirQualityApi();
        final BotHandler botHandler = new BotHandler(chatStates);
        botHandler
                .addCommand(new StartCommand(chatStates))
                .addCommand(new SetLocationCommand(chatStates, airQualityApi))
                .addCommand(new GetAirQualityCommand(airQualityApi, new AirQualityIndexProvider(), chatStates));
        return botHandler;
    }

    public static void main(String[] args) {
        logger.info("Starting the bot");
        String botApiKey = args.length > 0 ? args[0] : System.getenv("TELEGRAM_BOT_KEY");
        if (Strings.isNullOrEmpty(botApiKey)) {
            logger.error("Specify TELEGRAM_BOT_KEY by defining env variable or providing an argument");
            System.exit(-1);
        } else {
            new Main(TelegramBotAdapter.build(botApiKey), new Storage(new File(STATE_STORAGE_FILE_PATH))).start();
        }
    }

}
