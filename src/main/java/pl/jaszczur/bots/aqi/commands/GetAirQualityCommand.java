package pl.jaszczur.bots.aqi.commands;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import io.reactivex.Single;
import pl.jaszczur.bots.aqi.ChatState;
import pl.jaszczur.bots.aqi.ChatStates;
import pl.jaszczur.bots.aqi.UseCase;
import pl.jaszczur.bots.aqi.aqlogic.AirQualityApi;
import pl.jaszczur.bots.aqi.aqlogic.AirQualityIndexProvider;
import pl.jaszczur.bots.aqi.aqlogic.AirQualityResult;
import pl.jaszczur.bots.aqi.aqlogic.PartType;

import java.util.EnumSet;
import java.util.Set;

public class GetAirQualityCommand implements Command {
    private final AirQualityApi airQualityApi;
    private final AirQualityIndexProvider aqiProvider;
    private final ChatStates chatStates;

    public GetAirQualityCommand(AirQualityApi airQualityApi, AirQualityIndexProvider aqiProvider, ChatStates chatStates) {
        this.airQualityApi = airQualityApi;
        this.aqiProvider = aqiProvider;
        this.chatStates = chatStates;
    }

    @Override
    public Single<? extends BaseRequest<?, ? extends BaseResponse>> handle(Message message) {
        Chat chat = message.chat();
        ChatState chatState = chatStates.getState(chat);
        Long stationId = chatState.getStationId();
        if (stationId == null) {
            return Single.just(createMessage(chat, "Nie ustawiłeś/aś jeszcze stacji"));
        } else {
            return airQualityApi.getStats(stationId)
                    .map(aqi -> createMessage(chat, formatMessage(aqi)))
                    .onErrorReturn(err -> {
                        chatState.setUseCase(UseCase.SETTING_LOCATION);
                        return createMessage(chat, "Coś nie bangla. Chyba podałeś/aś niepoprawny numer stacji");
                    });
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

    private SendMessage createMessage(Chat chat, String text) {
        return new SendMessage(chat.id(), text)
                .parseMode(ParseMode.Markdown)
                .replyMarkup(new ReplyKeyboardMarkup(new String[]{"Podaj aktualne wartości"}));
    }

    private String formatMessage(AirQualityResult airQualityResult) {
        StringBuilder result = new StringBuilder(airQualityResult.getStation().getName());
        result.append("\n");
        for (PartType partType : airQualityResult.getAvailableParticleTypes()) {
            double value = airQualityResult.getValue(partType).get();
            result.append(" - ")
                    .append(partType)
                    .append(": *")
                    .append(value)
                    .append(" µg/m³* ")
                    .append(aqiProvider.get(partType, value).getUiIndicator())
                    .append("\n");
        }
        return result.toString();
    }
}
