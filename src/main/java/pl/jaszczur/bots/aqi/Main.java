package pl.jaszczur.bots.aqi;

import com.google.common.base.Strings;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import io.reactivex.Flowable;
import io.reactivex.flowables.ConnectableFlowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.jaszczur.bots.aqi.aqlogic.AirQualityApi;
import pl.jaszczur.bots.aqi.aqlogic.AirQualityIndexProvider;
import pl.jaszczur.bots.aqi.commands.GetAirQualityCommand;
import pl.jaszczur.bots.aqi.commands.SetLocationCommand;
import pl.jaszczur.bots.aqi.commands.StartCommand;
import pl.jaszczur.bots.aqi.commands.UpdateAirQualityCommand;
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
        storage.load().subscribe(
                chatStates -> {
                    final BotHandler botHandler = createBotHandler(chatStates);
                    bot.setUpdatesListener(updates -> processUpdates(botHandler, updates));
                },
                err -> logger.error(err.getMessage(), err));
    }

    private int processUpdates(BotHandler botHandler, List<Update> updates) {
        ConnectableFlowable<Update> updatesFlow = Flowable.fromIterable(updates).publish();

        updatesFlow
                .filter(u -> u.message() != null)
                .map(Update::message)
                .flatMap(botHandler::handle)
                .map(bot::execute)
                .subscribe(
                        msg -> {
                            if (msg.isOk())
                                logger.debug("Reply sent");
                            else
                                logger.warn("Error occurred {}: {}", msg.errorCode(), msg.description());
                        },
                        err -> logger.error("What a Terrible Failure", err));
        updatesFlow
                .filter(u -> u.callbackQuery() != null)
                .map(Update::callbackQuery)
                .flatMap(botHandler::handle)
                .map(bot::execute)
                .subscribe(); // TODO handle errors and log

        updatesFlow.connect();

        return UpdatesListener.CONFIRMED_UPDATES_ALL;

    }

    private BotHandler createBotHandler(ChatStates chatStates) {
        final AirQualityApi airQualityApi = new AirQualityApi();
        final BotHandler botHandler = new BotHandler(chatStates);
        AirQualityMessageProvider airQualityMessageProvider = new AirQualityMessageProvider(airQualityApi, new AirQualityIndexProvider());
        botHandler
                .addMessageCommand(new StartCommand(chatStates))
                .addMessageCommand(new SetLocationCommand(chatStates, airQualityApi))
                .addMessageCommand(new GetAirQualityCommand(airQualityMessageProvider, chatStates));
        botHandler
                .addCallbackCommand(new UpdateAirQualityCommand(airQualityMessageProvider, chatStates));
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
