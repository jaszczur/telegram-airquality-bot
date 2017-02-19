package pl.jaszczur.bots.aqi.commands;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import io.reactivex.Single;
import pl.jaszczur.bots.aqi.ChatState;
import pl.jaszczur.bots.aqi.ChatStates;
import pl.jaszczur.bots.aqi.TextCommands;
import pl.jaszczur.bots.aqi.UseCase;

import java.util.EnumSet;
import java.util.Set;

public class StartCommand implements Command {
    private final ChatStates states;

    public StartCommand(ChatStates states) {
        this.states = states;
    }

    @Override
    public Single<? extends BaseRequest<?, ? extends BaseResponse>> handle(Message msg) {
        ChatState chatState = states.getState(msg.chat());
        chatState.setUseCase(UseCase.SETTING_LOCATION);
        SendMessage reply = new SendMessage(msg.chat().id(), TextCommands.getText(chatState.getLocale(), "msg.hello"));
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
