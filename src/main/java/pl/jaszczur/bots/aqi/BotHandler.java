package pl.jaszczur.bots.aqi;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.BaseRequest;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

import java.util.List;
import java.util.Optional;

public class BotHandler {
    private final TelegramBot bot;
    private final ChatStates chatStates;
    private final List<Command> allCommands = Lists.newArrayList();

    public BotHandler(TelegramBot bot, ChatStates chatStates) {
        this.bot = bot;
        this.chatStates = chatStates;
    }

    public BotHandler addCommand(Command cmd) {
        allCommands.add(cmd);
        return this;
    }

    public boolean handle(Message msg) {
        UseCase useCase = chatStates.getState(msg.chat()).getUseCase();
        Optional<Command> command = allCommands.stream()
                .filter(cmd -> cmd.availableUseCases().contains(useCase))
                .filter(cmd -> cmd.canHandle(msg))
                .findFirst();
        Single<Command> cmd = command.map(Single::just).orElse(Single.never());
        cmd.flatMap(c -> c.handle(msg)).subscribe((Consumer<BaseRequest<?, ?>>) bot::execute);
        return command.isPresent();
    }

}
