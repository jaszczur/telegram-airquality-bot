package pl.jaszczur.bots.aqi;

import java.util.Optional;
import java.util.stream.Stream;

public enum PartType {
    PM25("PM2,5"), PM10("PM10");

    public static Optional<PartType> getByApiName(String apiName) {
        return Stream.of(PartType.values()).filter(pt -> pt.getApiName().equals(apiName)).findFirst();
    }

    private final String apiName;

    PartType(String apiName) {
        this.apiName = apiName;
    }

    public String getApiName() {
        return apiName;
    }
}
