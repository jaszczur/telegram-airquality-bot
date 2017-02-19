package pl.jaszczur.bots.aqi.commands;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import io.reactivex.Single;
import pl.jaszczur.bots.aqi.ChatState;
import pl.jaszczur.bots.aqi.ChatStates;
import pl.jaszczur.bots.aqi.UseCase;

import java.util.EnumSet;
import java.util.Set;

import static pl.jaszczur.bots.aqi.BotUtils.isCommand;
import static pl.jaszczur.bots.aqi.BotUtils.textWithoutCommand;

public class SetLocationCommand implements Command {
    public static final String COMMAND = "/set_station";
    private ChatStates chatStates;

    public SetLocationCommand(ChatStates chatStates) {
        this.chatStates = chatStates;
    }

    @Override
    public Single<? extends BaseRequest<?, ? extends BaseResponse>> handle(Message message) {
        Chat chat = message.chat();
        ChatState chatState = chatStates.getState(chat);
        try {
            long stationId = Long.parseLong(textWithoutCommand(message).get());
            chatState.setStationId(stationId);
            chatState.setUseCase(UseCase.GETTING_UPDATES);
            return Single.just(new SendMessage(chat.id(), "Ustawione :)"));
        } catch (NumberFormatException ex) {
            chatState.setUseCase(UseCase.SETTING_LOCATION);
            return Single.just(new SendMessage(chat.id(),
                    "Podaj numer stacji.\n" +
                            "Wyszukiwarka stacji znajduje się na stronie [http://powietrze.gios.gov.pl/pjp/station/search]. " +
                            "Po kliknięciu w wybraną stację jej numer pokaże się w adresie URL (na końcu, np 117).")
                    .parseMode(ParseMode.Markdown));
        }
    }

    @Override
    public boolean canHandle(Message msg) {
        UseCase useCase = chatStates.getState(msg.chat()).getUseCase();
        return useCase == UseCase.SETTING_LOCATION || isCommand(msg, COMMAND);
    }

    @Override
    public Set<UseCase> availableUseCases() {
        return EnumSet.of(UseCase.SETTING_LOCATION, UseCase.GETTING_UPDATES);
    }
}
