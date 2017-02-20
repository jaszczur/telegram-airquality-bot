package pl.jaszczur.bots.aqi.state;

import com.google.common.collect.ImmutableMap;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.jaszczur.bots.aqi.UseCase;
import pl.jaszczur.bots.aqi.aqlogic.Station;

import javax.json.*;
import java.io.*;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Storage {
    private static final Logger logger = LoggerFactory.getLogger(Storage.class);
    private final File file;

    public Storage(File file) {
        this.file = file;
    }

    public Single<ChatStates> load() {
        return Single.defer(() -> {
//            ChatStates chatStates = loadFromFile();
//            savePeriodically(chatStates);
            return Single.just(new ChatStates());
        });
    }

    private void savePeriodically(ChatStates chatStates) {
        Flowable.interval(20, 60, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(whatever -> saveToFile(chatStates));
    }

    private ChatStates loadFromFile() {
        ChatStates result = new ChatStates();
        try (InputStream is = new FileInputStream(file);
             JsonReader json = Json.createReader(is)) {
            JsonObject root = json.readObject();
            JsonArray jsonChatStates = root.getJsonArray("chatStates");

            ImmutableMap.Builder<Long, ChatState> statesBuilder = ImmutableMap.builder();

            for (JsonValue jsonChatStateValue : jsonChatStates) {
                JsonObject jsonChatState = (JsonObject) jsonChatStateValue;
                long chatId = jsonChatState.getJsonNumber("chatId").longValue();
                UseCase useCase = UseCase.valueOf(jsonChatState.getString("useCase"));
                String lang = jsonChatState.getString("language");

                JsonObject jsonStation = jsonChatState.getJsonObject("station");
                long stationId = jsonStation.getJsonNumber("id").longValue();
                String stationName = jsonStation.getString("name");
                Station station = new Station(stationId, stationName);

                ChatState chatState = new ChatState();
                chatState.setStation(station);
                chatState.setUseCase(useCase);
                chatState.setLanguage(new Locale(lang));

                statesBuilder.put(chatId, chatState);
            }

            result.setAll(statesBuilder.build());
        } catch (IOException e) {
            logger.warn("Error while restoring state from file {}. Creating new state.", file, e);
        }
        return result;
    }

    private void saveToFile(ChatStates chatStates) {
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for (Map.Entry<Long, ChatState> chatStateEntry : chatStates.getAll().entrySet()) {
            JsonObject jsonStation = Json.createObjectBuilder()
                    .add("id", chatStateEntry.getValue().getStation().getId())
                    .add("name", chatStateEntry.getValue().getStation().getId())
                    .build();

            JsonObject jsonChatState = Json.createObjectBuilder()
                    .add("chatId", chatStateEntry.getKey())
                    .add("useCase", chatStateEntry.getValue().getUseCase().name())
                    .add("language", chatStateEntry.getValue().getLocale().getLanguage())
                    .add("station", jsonStation)
                    .build();

            jsonArrayBuilder.add(jsonChatState);
        }
        JsonObject root = Json.createObjectBuilder().add("chatStates", jsonArrayBuilder.build()).build();

        try (OutputStream is = new FileOutputStream(file);
             JsonWriter json = Json.createWriter(is)) {
            json.writeObject(root);
        } catch (IOException e) {
            logger.warn("Error while storing chat states to file {}.", file, e);
        }
    }
}
