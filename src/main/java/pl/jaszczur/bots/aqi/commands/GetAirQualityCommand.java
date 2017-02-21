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
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.jaszczur.bots.aqi.AirQualityMessageProvider;
import pl.jaszczur.bots.aqi.BotUtils;
import pl.jaszczur.bots.aqi.UseCase;
import pl.jaszczur.bots.aqi.aqlogic.Station;
import pl.jaszczur.bots.aqi.state.ChatState;
import pl.jaszczur.bots.aqi.state.ChatStates;

import java.util.EnumSet;
import java.util.Set;

public class GetAirQualityCommand implements Command<Message> {
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
            return aqMessageProvider.getMessage(chatState.getLocale(), station.getId())
                    .map(msg -> createSuccessMessage(chat, chatState, msg))
                    .onErrorReturn(err -> {
                        logger.warn("Error while sending aq message", err);
                        return createFailureMessage(chat, chatState, "Coś nie bangla. Chyba podana stacja nie istnieje \uD83D\uDE14");
                    }).toFlowable();
        }

    }

    @Override
    public boolean canHandle(Message msg) {
        return true;
    }

    @Override
    public Set<UseCase> availableUseCases() {
        return EnumSet.of(UseCase.GETTING_UPDATES);
    }

    private SendMessage createSuccessMessage(Chat chat, ChatState chatState, String text) {
        return new SendMessage(chat.id(), text)
                .parseMode(ParseMode.Markdown)
                .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{
                        new InlineKeyboardButton("Odświerz").callbackData(Long.toString(chatState.getStation().getId()))
                }));
    }


    private SendMessage createFailureMessage(Chat chat, ChatState chatState, String text) {
        return new SendMessage(chat.id(), text)
                .parseMode(ParseMode.Markdown)
                .replyMarkup(BotUtils.getDefaultKeyboard(chatState.getLocale()));
    }
}
