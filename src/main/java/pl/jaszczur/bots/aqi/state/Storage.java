package pl.jaszczur.bots.aqi.state;

import io.reactivex.Single;

import java.io.File;

public class Storage {
    private final File file;

    public Storage(File file) {
        this.file = file;
    }

    public Single<ChatStates> load() {
        return Single.defer(() -> {
            ChatStates chatStates = new ChatStates();
            savePeriodically(chatStates);
            return Single.just(chatStates);
        });
    }

    private void savePeriodically(ChatStates chatStates) {
    }
}
