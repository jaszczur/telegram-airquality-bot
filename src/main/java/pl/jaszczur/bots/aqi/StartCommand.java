package pl.jaszczur.bots.aqi;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import io.reactivex.Single;

import java.util.EnumSet;
import java.util.Set;

public class StartCommand implements Command {
    private final ChatStates states;

    public StartCommand(ChatStates states) {
        this.states = states;
    }

    @Override
    public Single<BaseRequest<?, ?>> handle(Message msg) {
        SendMessage reply = new SendMessage(msg.chat().id(), "Siema. Najpierw proponuję ustawić swoją lokalizację.");
        states.getState(msg.chat()).setUseCase(UseCase.SETTING_LOCATION);
        return Single.just(reply);
    }

    @Override
    public boolean canHandle(Message msg) {
        return true;
    }

    @Override
    public Set<UseCase> availableUseCases() {
        return EnumSet.of(UseCase.NONE);
    }
}
