package pl.jaszczur.bots.aqi;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import io.reactivex.Single;

public class StartCommand implements Command {
    @Override
    public Single<BaseRequest<?, ?>> handle(Message msg) {
        SendMessage reply = new SendMessage(msg.chat().id(), "Siema. Najpierw proponuję ustawić swoją lokalizację.");
        return Single.just(reply);
    }

    @Override
    public boolean canHandle(Message msg) {
        return msg.text().equals("/start");
    }
}
