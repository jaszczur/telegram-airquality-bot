package pl.jaszczur.bots.aqi.state;

import com.google.common.collect.ImmutableMap;
import com.pengrad.telegrambot.model.Chat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import pl.jaszczur.bots.aqi.UseCase;
import pl.jaszczur.bots.aqi.aqlogic.Station;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
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
                                .add("useCase", "GETTING_UPDATES")
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
        assertEquals(UseCase.GETTING_UPDATES, chatState.getUseCase());
        assertEquals(new Station(666, "Parzymiechy Dolne"), chatState.getStation());
    }

    @Test
    void convertToJson_shouldConvert_whenEmptyStation() {
        ChatState chatState6 = new ChatState();
        chatState6.setUseCase(UseCase.SETTING_LOCATION);
        ChatStates states = ChatStates.create(ImmutableMap.of(
                6L, chatState6
        ));

        JsonObject jsonObject = cut.convertToJson(states);

        JsonArray jsonArray = jsonObject.getJsonArray("chatStates");
        assertEquals(1, jsonArray.size());

        JsonObject jsonValue = (JsonObject) jsonArray.get(0);
        assertEquals(6, jsonValue.getInt("chatId"));
        assertEquals("SETTING_LOCATION", jsonValue.getString("useCase"));
        assertEquals("pl", jsonValue.getString("language"));
    }

    @Test
    void convertToJson_shouldConvert() {
        ChatState chatState6 = new ChatState();
        chatState6.setUseCase(UseCase.GETTING_UPDATES);
        chatState6.setLanguage(Locale.ENGLISH);
        chatState6.setStation(new Station(6669, "Sosnowiec"));
        ChatStates states = ChatStates.create(ImmutableMap.of(
                6L, chatState6
        ));

        JsonObject jsonObject = cut.convertToJson(states);

        JsonArray jsonArray = jsonObject.getJsonArray("chatStates");
        assertEquals(1, jsonArray.size());

        JsonObject jsonValue = (JsonObject) jsonArray.get(0);
        assertEquals(6, jsonValue.getInt("chatId"));
        assertEquals("GETTING_UPDATES", jsonValue.getString("useCase"));
        assertEquals("en", jsonValue.getString("language"));
        assertEquals(6669, jsonValue.getJsonObject("station").getInt("id"));
        assertEquals("Sosnowiec", jsonValue.getJsonObject("station").getString("name"));
    }
}