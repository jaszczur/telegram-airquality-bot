package pl.jaszczur.bots.aqi.commands;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import pl.jaszczur.bots.aqi.BotUtils;
import pl.jaszczur.bots.aqi.UseCase;
import pl.jaszczur.bots.aqi.aqlogic.AirQualityApi;
import pl.jaszczur.bots.aqi.aqlogic.Station;
import pl.jaszczur.bots.aqi.state.ChatState;
import pl.jaszczur.bots.aqi.state.ChatStates;

import java.util.EnumSet;
import java.util.Set;

import static pl.jaszczur.bots.aqi.BotUtils.*;

public class SetLocationCommand implements Command {
    private static final String COMMAND = "/set_station";
    private ChatStates chatStates;
    private AirQualityApi aqApi;

    public SetLocationCommand(ChatStates chatStates, AirQualityApi api) {
        this.chatStates = chatStates;
        aqApi = api;
    }

    @Override
    public Single<? extends BaseRequest<?, ? extends BaseResponse>> handle(Message message) {
        return Single.defer(() -> {
            Chat chat = message.chat();
            String text = textWithoutCommand(message).get();
            ChatState chatState = chatStates.getState(chat);

            UseCase previousUseCase = chatState.getUseCase();
            chatState.setUseCase(UseCase.SETTING_LOCATION);

            if (previousUseCase != UseCase.SETTING_LOCATION || text.isEmpty()) {
                return askForStationMessage(chat);
            } else {
                return tryToSetStation(chat, chatState, text);
            }
        });
    }

    private Single<SendMessage> askForStationMessage(Chat chat) {
        return Single.just(new SendMessage(chat.id(), "Podaj nazwę lub numer stacji"));
    }

    private SingleSource<? extends SendMessage> tryToSetStation(Chat chat, ChatState chatState, String text) {
        try {
            return setStationById(chat, chatState, text);
        } catch (NumberFormatException ex) {
            return findStationByName(chat, text);
        }
    }

    private SingleSource<? extends SendMessage> setStationById(Chat chat, ChatState chatState, String text) {
        long stationId = Long.parseLong(text);
        return aqApi.getStation(stationId)
                .map(station -> {
                    chatState.setStation(station);
                    chatState.setUseCase(UseCase.GETTING_UPDATES);
                    return new SendMessage(chat.id(), "Ustawiono stację " + station.getName()).replyMarkup(BotUtils.getDefaultKeyboard(chatState.getLocale()));
                })
                .onErrorReturn(err -> new SendMessage(chat.id(), "Nie znaleziono takiej stacji"));
    }

    private SingleSource<? extends SendMessage> findStationByName(Chat chat, String text) {
        if (text.length() < 3) {
            return Single.just(new SendMessage(chat.id(), "Podaj co najmniej 3 znaki nazwy stacji"));
        } else {
            return aqApi.getStations(text)
                    .map(stations -> new SendMessage(chat.id(), listStations(stations)).parseMode(ParseMode.Markdown))
                    .onErrorReturn(err -> new SendMessage(chat.id(), "Nie znaleziono takiej stacji"));
        }
    }

    private String listStations(Set<Station> stations) {
        StringBuilder result = new StringBuilder();
        result.append("Znaleziono następujące stacje:\n");
        for (Station station : stations) {
            result
                    .append("*")
                    .append(station.getId())
                    .append(".* ")
                    .append(station.getName())
                    .append("\n");
        }
        result.append("Podaj numer stacji");
        return result.toString();
    }

    @Override
    public boolean canHandle(Message msg) {
        ChatState chatState = chatStates.getState(msg.chat());
        UseCase useCase = chatState.getUseCase();
        return useCase == UseCase.SETTING_LOCATION
                || isCommand(msg, COMMAND)
                || isTextCommand(chatState.getLocale(), msg, "cmd.set_station");
    }

    @Override
    public Set<UseCase> availableUseCases() {
        return EnumSet.of(UseCase.SETTING_LOCATION, UseCase.GETTING_UPDATES);
    }
}
