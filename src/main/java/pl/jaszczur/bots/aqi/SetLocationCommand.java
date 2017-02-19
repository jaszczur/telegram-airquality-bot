package pl.jaszczur.bots.aqi;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import io.reactivex.Single;

import static pl.jaszczur.bots.aqi.BotUtils.isCommand;
import static pl.jaszczur.bots.aqi.BotUtils.textWithoutCommand;

public class SetLocationCommand implements Command {
    public static final String COMMAND = "/set_station";
    private BotState botState;

    public SetLocationCommand(BotState botState) {
        this.botState = botState;
    }

    @Override
    public Single<BaseRequest<?, ?>> handle(Message message) {
        Chat chat = message.chat();
        try {
            long stationId = Long.parseLong(textWithoutCommand(message).get());
            botState.getStationIdByChatId().put(chat.id(), stationId);
            return Single.just(new SendMessage(chat.id(), "Ustawione :)"));
        } catch (NumberFormatException ex) {

            return Single.just(new SendMessage(chat.id(), "I to ma być numer? Podaj numer stacji, np 117"));
        }
    }

    @Override
    public boolean canHandle(Message msg) {
        return isCommand(msg, COMMAND);
    }

}
