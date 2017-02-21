package pl.jaszczur.bots.aqi.commands;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.response.BaseResponse;
import io.reactivex.Flowable;
import pl.jaszczur.bots.aqi.state.ChatStates;

public class UpdateAirQualityCommand implements Command<CallbackQuery> {
    private final AirQualityMessageProvider airQualityMessageProvider;
    private final ChatStates chatStates;

    public UpdateAirQualityCommand(AirQualityMessageProvider airQualityMessageProvider, ChatStates chatStates) {
        this.airQualityMessageProvider = airQualityMessageProvider;
        this.chatStates = chatStates;
    }

    @Override
    public Flowable<? extends BaseRequest<?, ? extends BaseResponse>> handle(CallbackQuery cq) {
        return Flowable.defer(() -> {
            long stationId = Long.parseLong(cq.data());
            Message attachedMessage = cq.message();
            return airQualityMessageProvider.getMessage(chatStates.getState(attachedMessage.chat()).getLocale(), stationId)
                    .toFlowable()
                    .flatMap(text -> {

                        EditMessageText editMessage = new EditMessageText(attachedMessage.chat().id(), attachedMessage.messageId(), text)
                                .parseMode(ParseMode.Markdown)
                                .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{
                                        new InlineKeyboardButton("Odśwież").callbackData(Long.toString(stationId))
                                }));
                        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(cq.id()).text("Pobrano aktualne dane");
                        return Flowable.just(editMessage, answerCallbackQuery);
                    });
        });
    }

    @Override
    public boolean canHandle(CallbackQuery cq) {
        try {
            Long.parseLong(cq.data());
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
