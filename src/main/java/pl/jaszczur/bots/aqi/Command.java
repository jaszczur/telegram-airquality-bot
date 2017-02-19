package pl.jaszczur.bots.aqi;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.BaseRequest;
import io.reactivex.Single;

public interface Command {
    Single<BaseRequest<?, ?>> handle(Message msg);

    boolean canHandle(Message msg);
}
