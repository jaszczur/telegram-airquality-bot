package pl.jaszczur.bots.aqi.commands;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.jaszczur.bots.aqi.BotUtils;
import pl.jaszczur.bots.aqi.UseCase;
import pl.jaszczur.bots.aqi.aqlogic.Station;
import pl.jaszczur.bots.aqi.state.ChatState;
import pl.jaszczur.bots.aqi.state.ChatStates;

import java.util.EnumSet;
import java.util.Set;

import static pl.jaszczur.bots.aqi.BotUtils.isCommand;
import static pl.jaszczur.bots.aqi.BotUtils.isTextCommand;

public class GetAirQualityCommand implements Command<Message> {
    static final String COMMAND = "/get";
    static final String TEXT_COMMAND = "cmd.refresh";
    private final AirQualityMessageProvider aqMessageProvider;
    private final ChatStates chatStates;
    private static final Logger logger = LoggerFactory.getLogger(GetAirQualityCommand.class);

    public GetAirQualityCommand(AirQualityMessageProvider aqMessageProvider, ChatStates chatStates) {
        this.aqMessageProvider = aqMessageProvider;
        this.chatStates = chatStates;
    }

    @Override
    public Flowable<? extends BaseRequest<?, ? extends BaseResponse>> handle(Message message) {
        Chat chat = message.chat();
        ChatState chatState = chatStates.getState(chat);
        Station station = chatState.getStation();
        if (station == null) {
            return Flowable.just(createFailureMessage(chat, chatState, "Nie ustawiłeś/aś jeszcze stacji"));
        } else {
            return aqMessageProvider.getMessage(chat, chatState).
                    onErrorReturn(err -> createFailureMessage(chat, chatState, "Coś nie bangla. Chyba podana stacja nie istnieje \uD83D\uDE14"));
        }

    }

    @Override
    public boolean canHandle(Message msg) {
        return canHandle(chatStates, msg);
    }

    @Override
    public Set<UseCase> availableUseCases() {
        return EnumSet.of(UseCase.GETTING_UPDATES, UseCase.SETTING_LOCATION);
    }



    private SendMessage createFailureMessage(Chat chat, ChatState chatState, String text) {
        return new SendMessage(chat.id(), text)
                .parseMode(ParseMode.Markdown)
                .replyMarkup(BotUtils.getDefaultKeyboard(chatState.getLocale()));
    }


    static boolean canHandle(ChatStates chatStates, Message msg) {
        ChatState chatState = chatStates.getState(msg.chat());
        return isCommand(msg, COMMAND)
                || isTextCommand(chatState.getLocale(), msg, TEXT_COMMAND);
    }
}
