package pl.jaszczur.bots.aqi;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.pengrad.telegrambot.model.Chat;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ChatStates {
    private Cache<Long, ChatState> states = CacheBuilder.newBuilder()
            .expireAfterAccess(14, TimeUnit.DAYS)
            .maximumSize(10 * 1024 * 1024)
            .build();

    public ChatState getState(Chat chat) {
        try {
            return states.get(chat.id(), ChatState::new);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
