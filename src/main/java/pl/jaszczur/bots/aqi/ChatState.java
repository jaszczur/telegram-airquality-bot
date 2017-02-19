package pl.jaszczur.bots.aqi;

import pl.jaszczur.bots.aqi.aqlogic.Station;

public class ChatState {
    private Station station;
    private UseCase useCase = UseCase.NONE;

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
}
