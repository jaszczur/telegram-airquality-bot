package pl.jaszczur.bots.aqi;

import com.google.common.collect.Lists;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.jaszczur.bots.aqi.commands.Command;
import pl.jaszczur.bots.aqi.state.ChatStates;

import java.util.List;
import java.util.Optional;

public class BotHandler {
    private static final Logger logger = LoggerFactory.getLogger(BotHandler.class);
    private final ChatStates chatStates;
    private final List<Command> allCommands = Lists.newArrayList();

    public BotHandler(ChatStates chatStates) {
        this.chatStates = chatStates;
    }

    public BotHandler addCommand(Command cmd) {
        allCommands.add(cmd);
        return this;
    }

    public Single<BaseRequest<?, ? extends BaseResponse>> handle(Message msg) {
        logger.debug("{}: Handling message \"{}\"", msg.chat().id(), msg.text());
        UseCase useCase = chatStates.getState(msg.chat()).getUseCase();
        Optional<Command> command = allCommands.stream()
                .filter(cmd -> cmd.availableUseCases().contains(useCase))
                .filter(cmd -> cmd.canHandle(msg))
                .findFirst();
        return command.map(Single::just).orElse(Single.never())
                .flatMap(c -> c.handle(msg));

    }

}
