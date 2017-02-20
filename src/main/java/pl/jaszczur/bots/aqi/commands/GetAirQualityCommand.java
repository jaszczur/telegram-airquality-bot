package pl.jaszczur.bots.aqi.commands;

import com.google.common.collect.Ordering;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.jaszczur.bots.aqi.BotUtils;
import pl.jaszczur.bots.aqi.TextCommands;
import pl.jaszczur.bots.aqi.UseCase;
import pl.jaszczur.bots.aqi.aqlogic.*;
import pl.jaszczur.bots.aqi.state.ChatState;
import pl.jaszczur.bots.aqi.state.ChatStates;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

public class GetAirQualityCommand implements Command {
    private final AirQualityApi airQualityApi;
    private final AirQualityIndexProvider aqiProvider;
    private final ChatStates chatStates;
    private static final Logger logger = LoggerFactory.getLogger(GetAirQualityCommand.class);

    public GetAirQualityCommand(AirQualityApi airQualityApi, AirQualityIndexProvider aqiProvider, ChatStates chatStates) {
        this.airQualityApi = airQualityApi;
        this.aqiProvider = aqiProvider;
        this.chatStates = chatStates;
    }

    @Override
    public Single<? extends BaseRequest<?, ? extends BaseResponse>> handle(Message message) {
        Chat chat = message.chat();
        ChatState chatState = chatStates.getState(chat);
        Station station = chatState.getStation();
        if (station == null) {
            return Single.just(createMessage(chat, chatState, "Nie ustawiłeś/aś jeszcze stacji"));
        } else {
            return airQualityApi.getStats(station.getId())
                    .map(aqi -> createMessage(chat, chatState, formatMessage(chatState.getLocale(), aqi)))
                    .onErrorReturn(err -> {
                        logger.warn("Error while sending aq message", err);
                        return createMessage(chat, chatState, "Coś nie bangla. Chyba podana stacja nie istnieje \uD83D\uDE14");
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

    private SendMessage createMessage(Chat chat, ChatState chatState, String text) {
        return new SendMessage(chat.id(), text)
                .parseMode(ParseMode.Markdown)
                .replyMarkup(BotUtils.getDefaultKeyboard(chatState.getLocale()));
    }

    private String formatMessage(Locale locale, AirQualityResult airQualityResult) {
        StringBuilder result = new StringBuilder(airQualityResult.getStation().getName());
        result.append("\n");
        for (PartType partType : Ordering.usingToString().sortedCopy(airQualityResult.getAvailableParticleTypes())) {
            double value = airQualityResult.getValue(partType).get();
            result.append(" - ")
                    .append(partType.getUiName())
                    .append(": *")
                    .append(String.format(locale, "%.1f", value))
                    .append(" µg/m³* ")
                    .append(TextCommands.getText(locale, aqiProvider.get(partType, value).getUiIndicator()))
                    .append("\n");
        }
        return result.toString();
    }
}
