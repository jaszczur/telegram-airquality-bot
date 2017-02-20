package pl.jaszczur.bots.aqi.state;

import com.google.common.collect.ImmutableMap;
import pl.jaszczur.bots.aqi.UseCase;
import pl.jaszczur.bots.aqi.aqlogic.Station;

import javax.json.*;
import java.util.Locale;
import java.util.Map;

class Converter {

    ChatStates loadFromJson(JsonObject root) {
        JsonArray jsonChatStates = root.getJsonArray("chatStates");

        ImmutableMap.Builder<Long, ChatState> statesBuilder = ImmutableMap.builder();

        for (JsonValue jsonChatStateValue : jsonChatStates) {
            // TODO: handle nulls
            JsonObject jsonChatState = (JsonObject) jsonChatStateValue;
            long chatId = jsonChatState.getJsonNumber("chatId").longValue();
            UseCase useCase = UseCase.valueOf(jsonChatState.getString("useCase"));
            String lang = jsonChatState.getString("language");

            Station station = parseStation(jsonChatState.getJsonObject("station"));

            ChatState chatState = new ChatState();
            chatState.setStation(station);
            chatState.setUseCase(useCase);
            chatState.setLanguage(new Locale(lang));

            statesBuilder.put(chatId, chatState);
        }

        return ChatStates.create(statesBuilder.build());
    }

    private Station parseStation(JsonObject jsonStation) {
        if (jsonStation == null) {
            return null;
        } else {
            long stationId = jsonStation.getJsonNumber("id").longValue();
            String stationName = jsonStation.getString("name");
            return new Station(stationId, stationName);
        }
    }

    JsonObject convertToJson(ChatStates chatStates) {
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for (Map.Entry<Long, ChatState> chatStateEntry : chatStates.getAll().entrySet()) {
            ChatState chatState = chatStateEntry.getValue();
            JsonObject jsonStation = serializeStation(chatState.getStation());
            JsonObjectBuilder jsonStateBuilder = Json.createObjectBuilder()
                    .add("chatId", chatStateEntry.getKey())
                    .add("useCase", chatState.getUseCase().name())
                    .add("language", chatState.getLocale().getLanguage());
            if (jsonStation != null) {
                jsonStateBuilder.add("station", jsonStation);
            }

            jsonArrayBuilder.add(jsonStateBuilder.build());
        }
        return Json.createObjectBuilder().add("chatStates", jsonArrayBuilder.build()).build();
    }

    private JsonObject serializeStation(Station station) {
        return station == null
                ? null
                : Json.createObjectBuilder()
                .add("id", station.getId())
                .add("name", station.getName())
                .build();
    }
}
