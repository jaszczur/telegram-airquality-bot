package pl.jaszczur.bots.aqi;

import com.google.common.collect.Lists;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.jaszczur.bots.aqi.commands.Command;
import pl.jaszczur.bots.aqi.state.ChatStates;

import java.util.List;
import java.util.Optional;

public class BotHandler {
    private static final Logger logger = LoggerFactory.getLogger(BotHandler.class);
    private final ChatStates chatStates;
    private final List<Command<Message>> messageCommands = Lists.newArrayList();
    private final List<Command<CallbackQuery>> callbackCommands = Lists.newArrayList();

    public BotHandler(ChatStates chatStates) {
        this.chatStates = chatStates;
    }

    public BotHandler addMessageCommand(Command<Message> cmd) {
        messageCommands.add(cmd);
        return this;
    }

    public BotHandler addCallbackCommand(Command<CallbackQuery> cmd) {
        callbackCommands.add(cmd);
        return this;
    }

    public Flowable<BaseRequest<?, ? extends BaseResponse>> handle(Message msg) {
        return Flowable.defer(() -> {
            logger.debug("{}: Handling message \"{}\"", msg.chat().id(), msg.text());
            UseCase useCase = chatStates.getState(msg.chat()).getUseCase();
            Optional<Command<Message>> command = messageCommands.stream()
                    .filter(cmd -> cmd.availableUseCases().contains(useCase))
                    .filter(cmd -> cmd.canHandle(msg))
                    .findFirst();
            return command.map(Flowable::just).orElse(Flowable.never())
                    .flatMap(c -> c.handle(msg));
        });
    }

    public Flowable<BaseRequest<?, ? extends BaseResponse>> handle(CallbackQuery cq) {
        return Flowable.defer(() -> {
            Chat chat = cq.message().chat();
            logger.debug("{}: Handling callback \"{}\"", chat.id(), cq.data());
            UseCase useCase = chatStates.getState(chat).getUseCase();
            Optional<Command<CallbackQuery>> command = callbackCommands.stream()
                    .filter(cmd -> cmd.availableUseCases().contains(useCase))
                    .filter(cmd -> cmd.canHandle(cq))
                    .findFirst();
            return command.map(Flowable::just).orElse(Flowable.never())
                    .flatMap(c -> c.handle(cq));
        });
    }
}
