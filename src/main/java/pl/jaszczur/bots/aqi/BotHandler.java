package pl.jaszczur.bots.aqi;

import com.google.common.collect.Lists;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

import java.util.List;
import java.util.Optional;

public class BotHandler {
    private final TelegramBot bot;
    private List<Command> commands = Lists.newArrayList();

    public BotHandler(TelegramBot bot) {
        this.bot = bot;
    }

    public BotHandler addCommand(Command cmd) {
        commands.add(cmd);
        return this;
    }

    public boolean handle(Message msg) {
        Optional<Command> command = commands.stream().filter(cmd -> cmd.canHandle(msg)).findFirst();
        Single<Command> cmd = command.map(Single::just).orElse(Single.never());
        cmd.flatMap(c -> c.handle(msg)).subscribe((Consumer<BaseRequest<?, ?>>) bot::execute);
        return command.isPresent();
    }
}
