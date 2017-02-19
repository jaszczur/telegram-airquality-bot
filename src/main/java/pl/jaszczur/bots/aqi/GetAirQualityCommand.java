package pl.jaszczur.bots.aqi;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import java.util.EnumSet;
import java.util.Set;

public class GetAirQualityCommand implements Command {
    private final AirQualityApi airQualityApi;
    private final ChatStates chatStates;

    public GetAirQualityCommand(AirQualityApi airQualityApi, ChatStates chatStates) {
        this.airQualityApi = airQualityApi;
        this.chatStates = chatStates;
    }

    @Override
    public Single<BaseRequest<?, ?>> handle(Message message) {
        Chat chat = message.chat();
        Long stationId = chatStates.getState(chat).getStationId();
        if (stationId == null) {
            return Single.just(createMessage(chat, "Nie ustawiłeś/aś jeszcze stacji"));
        } else {
            return checkAirQuality(chat, stationId, aqi -> createMessage(chat, formatMessage(aqi)));
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

    private Single<BaseRequest<?, ?>> checkAirQuality(Chat chat, long stationId, Function<AirQualityResult, BaseRequest<?, ?>> airQualityResultConsumer) {
        return airQualityApi.getStats(stationId)
                .map(airQualityResultConsumer)
                .onErrorReturn(err -> createMessage(chat, "Coś nie bangla. Chyba podałeś/aś niepoprawny numer stacji"));
    }

    private SendMessage createMessage(Chat chat, String text) {
        return new SendMessage(chat.id(), text)
                .parseMode(ParseMode.Markdown)
                .disableWebPagePreview(true)
                .disableNotification(true);
    }

    private String formatMessage(AirQualityResult airQualityResult) {
        return airQualityResult.getStation().getName() + ":\n"
                + " - Pyłki PM2.5: *" + airQualityResult.getValues().get(PartType.PM25) + " µg/m³*\n"
                + " - Pyłki PM10: *" + airQualityResult.getValues().get(PartType.PM10) + " µg/m³*\n";
    }
}
