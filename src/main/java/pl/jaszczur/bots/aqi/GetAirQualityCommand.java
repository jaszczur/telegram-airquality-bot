package pl.jaszczur.bots.aqi;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import io.reactivex.Single;
import io.reactivex.functions.Function;

public class GetAirQualityCommand implements Command {
    private final AirQualityApi airQualityApi;
    private final BotState state;

    public GetAirQualityCommand(AirQualityApi airQualityApi, BotState state) {
        this.airQualityApi = airQualityApi;
        this.state = state;
    }

    @Override
    public Single<BaseRequest<?, ?>> handle(Message message) {
        Chat chat = message.chat();
        try {
            long stationId = Long.parseLong(message.text());
            return checkAirQuality(chat, stationId, aqi -> {
                state.getStationIdByChatId().put(chat.id(), stationId);
                return createMessage(chat, formatMessage(aqi));
            });
        } catch (NumberFormatException ex) {
            Long stationId = state.getStationIdByChatId().get(chat.id());
            if (stationId == null) {
                return Single.just(createMessage(chat, "Podaj numer stacji, np 117"));
            } else {
                return checkAirQuality(chat, stationId, aqi -> createMessage(chat, formatMessage(aqi)));
            }
        }
    }

    @Override
    public boolean canHandle(Message msg) {
        return true;
    }


    private Single<BaseRequest<?, ?>> checkAirQuality(Chat chat, long stationId, Function<AirQualityResult, BaseRequest<?, ?>> airQualityResultConsumer) {
        return airQualityApi.getStats(stationId)
                .map(airQualityResultConsumer)
                .onErrorReturn(err -> createMessage(chat, "Coś nie bangla. Chyba podałeś/aś numer stacji z dupy..."));
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
