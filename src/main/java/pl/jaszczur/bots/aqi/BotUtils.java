package pl.jaszczur.bots.aqi;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.MessageEntity;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

public final class BotUtils {
    private static final Logger logger = LoggerFactory.getLogger(BotUtils.class);

    private BotUtils() {
        // prevent creation
    }

    public static boolean isCommand(Message msg, String command) {
        return msg.entities() != null && Stream.of(msg.entities())
                .filter(ent -> ent.type() == MessageEntity.Type.bot_command)
                .map(ent -> msg.text().substring(ent.offset(), ent.length()))
                .anyMatch(command::equals);
    }

    public static boolean isTextCommand(Locale locale, Message msg, String textCommand) {
        return msg.text().equalsIgnoreCase(TextCommands.getText(locale, textCommand));
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

    public static Keyboard getDefaultKeyboard(Locale locale) {
        return new ReplyKeyboardMarkup(
                new String[] {TextCommands.getText(locale, "cmd.refresh")},
                new String[] {TextCommands.getText(locale, "cmd.set_station")})
                .oneTimeKeyboard(true);
    }

}
