package pl.jaszczur.bots.aqi.commands;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import io.reactivex.Flowable;
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
            return Flowable.just(
                    new SendMessage(chat.id(), "Nie ustawiłeś/aś jeszcze stacji").parseMode(ParseMode.Markdown));
        } else {
            return aqMessageProvider.getMessage(chat, chatState);
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


    static boolean canHandle(ChatStates chatStates, Message msg) {
        ChatState chatState = chatStates.getState(msg.chat());
        return isCommand(msg, COMMAND)
                || isTextCommand(chatState.getLocale(), msg, TEXT_COMMAND);
    }
}
