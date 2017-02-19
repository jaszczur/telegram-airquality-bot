package pl.jaszczur.bots.aqi;

public class ChatState {
    private Long stationId;
    private UseCase useCase = UseCase.NONE;

    public Long getStationId() {
        return stationId;
    }

    public void setStationId(Long stationId) {
        this.stationId = stationId;
    }

    public UseCase getUseCase() {
        return useCase;
    }

    public void setUseCase(UseCase useCase) {
        this.useCase = useCase;
    }
}
