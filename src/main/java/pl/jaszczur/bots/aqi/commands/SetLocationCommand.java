package pl.jaszczur.bots.aqi.commands;

import com.google.common.collect.Iterables;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.jaszczur.bots.aqi.TextCommands;
import pl.jaszczur.bots.aqi.UseCase;
import pl.jaszczur.bots.aqi.aqlogic.AirQualityApi;
import pl.jaszczur.bots.aqi.aqlogic.Station;
import pl.jaszczur.bots.aqi.state.ChatState;
import pl.jaszczur.bots.aqi.state.ChatStates;

import java.util.EnumSet;
import java.util.NoSuchElementException;
import java.util.Set;

import static pl.jaszczur.bots.aqi.BotUtils.textWithoutCommand;

public class SetLocationCommand implements Command<Message> {
    private static final String COMMAND = "/set_station";

    private static final Logger logger = LoggerFactory.getLogger(SetLocationCommand.class);

    private final ChatStates chatStates;
    private final AirQualityApi aqApi;
    private final AirQualityMessageProvider aqMessageProvider;

    public SetLocationCommand(ChatStates chatStates, AirQualityApi api, AirQualityMessageProvider aqMessageProvider) {
        this.chatStates = chatStates;
        aqApi = api;
        this.aqMessageProvider = aqMessageProvider;
    }

    @Override
    public Flowable<? extends BaseRequest<?, ? extends BaseResponse>> handle(Message message) {
        return Flowable.defer(() -> {
            Chat chat = message.chat();
            String text = textWithoutCommand(message).get();
            ChatState chatState = chatStates.getState(chat);

            UseCase previousUseCase = chatState.getUseCase();
            chatState.setUseCase(UseCase.SETTING_LOCATION);

            if (previousUseCase == UseCase.NONE || text.isEmpty()) {
                return askForStationMessage(chat).toFlowable();
            } else {
                return tryToSetStation(chat, chatState, text);
            }
        });
    }

    private Single<SendMessage> askForStationMessage(Chat chat) {
        return Single.just(new SendMessage(chat.id(), "Podaj nazwę lub numer stacji"));
    }

    private Flowable<SendMessage> tryToSetStation(Chat chat, ChatState chatState, String text) {
        try {
            return setStationById(chat, chatState, text);
        } catch (NumberFormatException ex) {
            return findStationByName(chat, chatState, text);
        }
    }

    private Flowable<SendMessage> setStationById(Chat chat, ChatState chatState, String text) {
        long stationId = Long.parseLong(text);
        return aqApi.getStation(stationId)
                .toFlowable()
                .flatMap(st -> setStationAndReply(chat, chatState, st))
                .onErrorReturn(err ->
                        new SendMessage(chat.id(),
                                err instanceof NoSuchElementException
                                        ? "Nie znaleziono takiej stacji"
                                        : TextCommands.getText(chatState.getLocale(), "msg.server_error")));
    }

    private Flowable<SendMessage> findStationByName(Chat chat, ChatState chatState, String text) {
        if (text.length() < 3) {
            return Flowable.just(new SendMessage(chat.id(), "Podaj co najmniej 3 znaki nazwy stacji"));
        } else {
            return aqApi.getStations(text)
                    .toFlowable()
                    .flatMap(stations -> {
                        if (stations.isEmpty())
                            return Flowable.just(new SendMessage(chat.id(), "Nie znaleziono takiej stacji"));
                        else if (stations.size() == 1) {
                            return setStationAndReply(chat, chatState, Iterables.getOnlyElement(stations));
                        } else
                            return Flowable.just(new SendMessage(chat.id(), listStations(stations)).parseMode(ParseMode.Markdown));
                    })
                    .onErrorReturn(err -> new SendMessage(chat.id(), TextCommands.getText(chatState.getLocale(), "msg.server_error")));
        }
    }

    private Flowable<SendMessage> setStationAndReply(Chat chat, ChatState chatState, Station station) {
        chatState.setStation(station);
        chatState.setUseCase(UseCase.GETTING_UPDATES);
        SendMessage confirmation = new SendMessage(chat.id(), "Ustawiono stację *" + station.getName() + "*.").parseMode(ParseMode.Markdown);

        return Flowable.just(confirmation)
                .mergeWith(aqMessageProvider.getMessage(chat, chatState));
    }

    private String listStations(Set<Station> stations) {
        StringBuilder result = new StringBuilder();
        result.append("Znaleziono następujące stacje:\n");
        for (Station station : stations) {
            result
                    .append("*")
                    .append(station.getId())
                    .append("*. ")
                    .append(station.getName())
                    .append("\n");
        }
        result.append("Podaj numer stacji");
        return result.toString();
    }

    @Override
    public boolean canHandle(Message msg) {
        return !GetAirQualityCommand.canHandle(chatStates, msg);
    }

    @Override
    public Set<UseCase> availableUseCases() {
        return EnumSet.of(UseCase.SETTING_LOCATION, UseCase.GETTING_UPDATES);
    }
}
