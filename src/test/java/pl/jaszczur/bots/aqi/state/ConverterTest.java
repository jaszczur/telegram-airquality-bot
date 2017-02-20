package pl.jaszczur.bots.aqi.state;

import com.pengrad.telegrambot.model.Chat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import pl.jaszczur.bots.aqi.UseCase;
import pl.jaszczur.bots.aqi.aqlogic.Station;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.File;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class ConverterTest {
    private final Converter cut = new Converter();
    private final Chat chat = mock(Chat.class);

    @Test
    void loadFromJson_shouldLoadEvenWhenStationIsNotChosen() {
        JsonObject example = Json.createObjectBuilder()
                .add("chatStates", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("chatId", 69L)
                                .add("language", "pl")
                                .add("useCase", "NONE")
                                .build())
                        .build())
                .build();
        given(chat.id()).willReturn(69L);

        ChatStates chatStates = cut.loadFromJson(example);
        ChatState chatState = chatStates.getState(chat);

        assertEquals(new Locale("pl"), chatState.getLocale());
        assertEquals(UseCase.NONE, chatState.getUseCase());
    }

    @Test
    void loadFromJson_shouldLoad() {
        JsonObject example = Json.createObjectBuilder()
                .add("chatStates", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("chatId", 69L)
                                .add("language", "pl")
                                .add("useCase", "NONE")
                                .add("station", Json.createObjectBuilder()
                                        .add("id", 666L)
                                        .add("name", "Parzymiechy Dolne")
                                        .build())
                                .build())
                        .build())
                .build();
        given(chat.id()).willReturn(69L);

        ChatStates chatStates = cut.loadFromJson(example);
        ChatState chatState = chatStates.getState(chat);

        assertEquals(new Locale("pl"), chatState.getLocale());
        assertEquals(UseCase.NONE, chatState.getUseCase());
        assertEquals(new Station(666, "Parzymiechy Dolne"), chatState.getStation());
    }



}