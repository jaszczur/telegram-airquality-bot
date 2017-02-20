package pl.jaszczur.bots.aqi.state;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import java.io.*;
import java.util.concurrent.TimeUnit;

public class Storage {
    private static final Logger logger = LoggerFactory.getLogger(Storage.class);
    private final Converter converter = new Converter();
    private final File file;

    public Storage(File file) {
        this.file = file;
    }

    public Single<ChatStates> load() {
        return Single.defer(() -> {
            ChatStates chatStates = loadFromFile();
            savePeriodically(chatStates);
            return Single.just(chatStates);
        });
    }

    private void savePeriodically(ChatStates chatStates) {
        Flowable.interval(20, 60, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(whatever -> saveToFile(chatStates));
    }

    private ChatStates loadFromFile() {
        ChatStates result;
        try (InputStream is = new FileInputStream(file);
             JsonReader json = Json.createReader(is)) {
            JsonObject root = json.readObject();
            result = converter.loadFromJson(root);
        } catch (IOException e) {
            result = ChatStates.create();
            logger.warn("Error while restoring state from file {}. Creating new state.", file, e);
        }
        return result;
    }

    private void saveToFile(ChatStates chatStates) {
        JsonObject root = converter.convertToJson(chatStates);

        try (OutputStream is = new FileOutputStream(file);
             JsonWriter json = Json.createWriter(is)) {
            json.writeObject(root);
        } catch (IOException e) {
            logger.warn("Error while storing chat states to file {}.", file, e);
        }
    }

}
