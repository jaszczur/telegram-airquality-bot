package pl.jaszczur.bots.aqi;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.MessageEntity;

import java.util.Optional;
import java.util.stream.Stream;

public final class BotUtils {
    private BotUtils() {
        // prevent creation
    }

    public static boolean isCommand(Message msg, String command) {
        return msg.entities() != null && Stream.of(msg.entities())
                .filter(ent -> ent.type() == MessageEntity.Type.bot_command)
                .map(ent -> msg.text().substring(ent.offset(), ent.length()))
                .map(cmd -> {
                    System.out.println(cmd);
                    return cmd;
                })
                .anyMatch(command::equals);
    }

    public static Optional<String> textWithoutCommand(Message msg) {
        return msg.entities() != null
                ? Stream.of(msg.entities())
                .filter(ent -> ent.type() == MessageEntity.Type.bot_command)
                .map(ent -> msg.text().substring(ent.offset() + ent.length()))
                .map(String::trim)
                .findFirst()
                : Optional.of(msg.text());
    }


}