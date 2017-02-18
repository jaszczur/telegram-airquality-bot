package pl.jaszczur.bots.aqi;

import com.google.common.collect.ImmutableMap;
import io.reactivex.Single;
import org.glassfish.jersey.client.rx.RxClient;
import org.glassfish.jersey.client.rx.java8.RxCompletionStage;
import org.glassfish.jersey.client.rx.java8.RxCompletionStageInvoker;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AirQualityApi {

    private final RxClient<RxCompletionStageInvoker> client = RxCompletionStage.newClient();

    public Single<AirQualityResult> getStats(long stationId) {
        CompletableFuture<Response> responseCompletionStage = client
                .target("http://powietrze.gios.gov.pl/pjp/current/getAQIDetails")
                .queryParam("id", stationId) //117
                .queryParam("param", "AQI")
                .request()
                .rx()
                .get().toCompletableFuture();
        return Single.fromFuture(responseCompletionStage)
                .map((resp) -> resp.readEntity(JsonObject.class))
                .map(this::parseResult);

    }

    private  AirQualityResult parseResult(JsonObject jsonObject) {
        Station station = new Station(0, jsonObject.getString("stationName"));
        JsonObject jsonValues = jsonObject.getJsonObject("values");
        ImmutableMap.Builder<PartType, Double> valuesBuilder = ImmutableMap.builder();

        for (Map.Entry<String, JsonValue> entry : jsonValues.entrySet()) {
            for (PartType partType : PartType.values()) {
                if (partType.getApiName().equals(entry.getKey())) {
                    valuesBuilder.put(partType, ((JsonNumber)entry.getValue()).doubleValue());
                }
            }
        }
        return new AirQualityResult(station, valuesBuilder.build());
    }

}
