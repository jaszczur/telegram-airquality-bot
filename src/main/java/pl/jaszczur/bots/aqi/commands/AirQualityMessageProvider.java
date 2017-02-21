package pl.jaszczur.bots.aqi.commands;

import com.google.common.collect.Ordering;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import io.reactivex.Flowable;
import io.reactivex.Single;
import pl.jaszczur.bots.aqi.TextCommands;
import pl.jaszczur.bots.aqi.aqlogic.AirQualityApi;
import pl.jaszczur.bots.aqi.aqlogic.AirQualityIndexProvider;
import pl.jaszczur.bots.aqi.aqlogic.AirQualityResult;
import pl.jaszczur.bots.aqi.aqlogic.PartType;
import pl.jaszczur.bots.aqi.state.ChatState;

import java.util.List;
import java.util.Locale;

public class AirQualityMessageProvider {
    private final AirQualityApi airQualityApi;
    private final AirQualityIndexProvider airQualityIndexProvider;

    public AirQualityMessageProvider(AirQualityApi airQualityApi, AirQualityIndexProvider airQualityIndexProvider) {
        this.airQualityApi = airQualityApi;
        this.airQualityIndexProvider = airQualityIndexProvider;
    }

    public Single<String> getMessage(Locale locale, long stationId) {
        return airQualityApi.getStats(stationId).map(aqr -> formatMessage(locale, aqr));
    }

    public Flowable<SendMessage> getMessage(Chat chat, ChatState chatState) {
        return getMessage(chatState.getLocale(), chatState.getStation().getId())
                .map(msg -> createSuccessMessage(chat, chatState, msg))
                .toFlowable();

    }

    private SendMessage createSuccessMessage(Chat chat, ChatState chatState, String text) {
        return new SendMessage(chat.id(), text)
                .parseMode(ParseMode.Markdown)
                .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{
                        new InlineKeyboardButton("Odśwież").callbackData(Long.toString(chatState.getStation().getId()))
                }));
    }

    private String formatMessage(Locale locale, AirQualityResult airQualityResult) {
        StringBuilder result = new StringBuilder();
        result.append("Aktualne poziomy dla stacji *").append(airQualityResult.getStation().getName()).append("*:\n");
        result.append("\n");
        for (PartType partType : sortedParticles(airQualityResult)) {
            double value = airQualityResult.getValue(partType);
            result.append("- ")
                    .append(partType.getUiName())
                    .append(": *")
                    .append(String.format(locale, "%.1f", value))
                    .append(" µg/m³* ")
                    .append(TextCommands.getText(locale, airQualityIndexProvider.get(partType, value).getUiIndicator()))
                    .append("\n");
        }
        return result.toString();
    }

    private List<PartType> sortedParticles(AirQualityResult airQualityResult) {
        return Ordering.usingToString().sortedCopy(airQualityResult.getAvailableParticleTypes());
    }
}
