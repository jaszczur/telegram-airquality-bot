package pl.jaszczur.bots.aqi;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import io.reactivex.Single;

import java.util.EnumSet;
import java.util.Set;

public interface Command {
    Single<? extends BaseRequest<?, ? extends BaseResponse>> handle(Message msg);
    boolean canHandle(Message msg);
    default Set<UseCase> availableUseCases() {
        return EnumSet.allOf(UseCase.class);
    }

}
