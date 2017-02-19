package pl.jaszczur.bots.aqi.aqlogic;

import java.util.Optional;
import java.util.stream.Stream;

public enum PartType {
    PM25("PM2,5", "PM 2,5"),
    PM10("PM10", "PM 10"),
    O3("O<sub>3</sub>", "0₃"),
    C6H6("C<sub>6</sub>H<sub>6</sub>", "C₆H₆"),
    CO("CO", "CO"),
    SO2("SO<sub>2</sub>", "SO₂"),
    NO2("NO<sub>2</sub>", "NO₂");

    public static Optional<PartType> getByApiName(String apiName) {
        return Stream.of(PartType.values()).filter(pt -> pt.getApiName().equals(apiName)).findFirst();
    }

    private final String apiName;
    private String uiName;

    PartType(String apiName, String uiName) {
        this.apiName = apiName;
        this.uiName = uiName;
    }

    public String getUiName() {
        return uiName;
    }

    String getApiName() {
        return apiName;
    }
}
