package pl.jaszczur.bots.aqi.aqlogic;

import java.util.Optional;
import java.util.stream.Stream;

public enum PartType {
    PM25("PM2,5", "PM 2,5", 1.0),
    PM10("PM10", "PM 10", 1.0),
    O3("O<sub>3</sub>", "0₃", 1.0),
    C6H6("C<sub>6</sub>H<sub>6</sub>", "C₆H₆", 1.0),
    CO("CO", "CO", 1000.0),
    SO2("SO<sub>2</sub>", "SO₂", 1.0),
    NO2("NO<sub>2</sub>", "NO₂", 1.0);

    public static Optional<PartType> getByApiName(String apiName) {
        return Stream.of(PartType.values()).filter(pt -> pt.getApiName().equals(apiName)).findFirst();
    }

    private final String apiName;
    private String uiName;
    private final double factor;

    PartType(String apiName, String uiName, double factor) {
        this.apiName = apiName;
        this.uiName = uiName;
        this.factor = factor;
    }

    public String getUiName() {
        return uiName;
    }

    String getApiName() {
        return apiName;
    }

    public double getFactor() {
        return factor;
    }
}
