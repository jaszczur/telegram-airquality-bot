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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.jaszczur.bots.aqi.TextCommands;
import pl.jaszczur.bots.aqi.state.ChatState;
import pl.jaszczur.bots.aqi.state.ChatStates;

public class UpdateAirQualityCommand implements Command<CallbackQuery> {
    private static final Logger logger = LoggerFactory.getLogger(UpdateAirQualityCommand.class);
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
            ChatState chatState = chatStates.getState(attachedMessage.chat());
            return airQualityMessageProvider.getMessage(chatState.getLocale(), stationId)
                    .toFlowable()
                    .flatMap(text -> {
                        EditMessageText editMessage = new EditMessageText(attachedMessage.chat().id(), attachedMessage.messageId(), text)
                                .parseMode(ParseMode.Markdown)
                                .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[]{
                                        new InlineKeyboardButton("Odśwież").callbackData(Long.toString(stationId))
                                }));
                        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(cq.id()).text("Pobrano aktualne dane");
                        return Flowable.just(editMessage, answerCallbackQuery);
                    })
                    .onErrorReturn((err) -> {
                        logger.warn("error", err);
                        return new AnswerCallbackQuery(cq.id()).text(TextCommands.getText(chatState.getLocale(), "msg.server_error"));
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
