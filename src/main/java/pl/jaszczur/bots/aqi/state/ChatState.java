package pl.jaszczur.bots.aqi.state;

import pl.jaszczur.bots.aqi.UseCase;
import pl.jaszczur.bots.aqi.aqlogic.Station;

import java.util.Locale;

public class ChatState {
    private Station station;
    private UseCase useCase = UseCase.NONE;
    private Locale language = Locale.forLanguageTag("pl_PL");

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public UseCase getUseCase() {
        return useCase;
    }

    public void setUseCase(UseCase useCase) {
        this.useCase = useCase;
    }

    public Locale getLocale() {
        return language;
    }

    public void setLanguage(Locale language) {
        this.language = language;
    }
}
