package pl.jaszczur.bots.aqi.state;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.pengrad.telegrambot.model.Chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ChatStates {
    private final Cache<Long, ChatState> states;

    static ChatStates create() {
        return create(Collections.emptyMap());
    }

    static ChatStates create(Map<Long, ChatState> predefinedStates) {
        Cache<Long, ChatState> states = CacheBuilder.newBuilder()
                .expireAfterAccess(14, TimeUnit.DAYS)
                .maximumSize(10 * 1024 * 1024L)
                .build();
        states.putAll(predefinedStates);
        return new ChatStates(states);
    }

    private ChatStates(Cache<Long, ChatState> states) {
        this.states = states;
    }

    public ChatState getState(Chat chat) {
        try {
            return states.get(chat.id(), ChatState::new);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    Map<Long, ChatState> getAll() {
        return ImmutableMap.copyOf(states.asMap());
    }
}
