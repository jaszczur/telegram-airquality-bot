package pl.jaszczur.bots.aqi;

import com.google.common.collect.Maps;
import com.pengrad.telegrambot.model.Chat;

import java.util.Map;

public class ChatStates {
    // TODO: change to expiring cache
    private Map<Long, ChatState> states = Maps.newHashMap();

    public ChatState getState(Chat chat) {
        return states.computeIfAbsent(chat.id(), k -> new ChatState());
    }
}
