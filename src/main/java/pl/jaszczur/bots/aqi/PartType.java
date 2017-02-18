package pl.jaszczur.bots.aqi;

/**
 * Created by jaszczur on 2017/2/18.
 */
public enum PartType {
    PM25("PM2,5"), PM10("PM10");

    private final String apiName;

    PartType(String apiName) {
        this.apiName = apiName;
    }

    public String getApiName() {
        return apiName;
    }
}
