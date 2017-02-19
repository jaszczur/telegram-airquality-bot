package pl.jaszczur.bots.aqi;

import com.google.common.collect.Maps;

import java.util.Map;

public class BotState {
    // TODO: change to expiring cache
    private Map<Long, Long> stationIdByChatId = Maps.newHashMap();

    public Map<Long, Long> getStationIdByChatId() {
        return stationIdByChatId;
    }
}
