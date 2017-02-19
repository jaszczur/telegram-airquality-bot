package pl.jaszczur.bots.aqi;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.BaseRequest;
import io.reactivex.Single;

import java.util.EnumSet;
import java.util.Set;

public interface Command {
    Single<BaseRequest<?, ?>> handle(Message msg);
    boolean canHandle(Message msg);
    default Set<UseCase> availableUseCases() {
        return EnumSet.allOf(UseCase.class);
    }

}
