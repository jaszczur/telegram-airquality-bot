package pl.jaszczur.bots.aqi.state;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.pengrad.telegrambot.model.Chat;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ChatStates {
    private Cache<Long, ChatState> states = CacheBuilder.newBuilder()
            .expireAfterAccess(14, TimeUnit.DAYS)
            .maximumSize(10 * 1024 * 1024L)
            .build();

    public ChatState getState(Chat chat) {
        try {
            return states.get(chat.id(), ChatState::new);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
