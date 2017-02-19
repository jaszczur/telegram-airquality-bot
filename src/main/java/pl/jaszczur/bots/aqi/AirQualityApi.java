package pl.jaszczur.bots.aqi;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class AirQualityApi {

    private final RxClient<RxCompletionStageInvoker> client = RxCompletionStage.newClient();
    private final LoadingCache<Long, Single<AirQualityResult>> aqResults = CacheBuilder.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build(new AirQualityDataLoader());

    public Single<AirQualityResult> getStats(long stationId) {

        return Single.defer(() -> {
            try {
                return aqResults.get(stationId);
            } catch (ExecutionException e) {
                return Single.error(e);
            }
        });
    }

    private AirQualityResult parseResult(JsonObject jsonObject) {
        Station station = new Station(0, jsonObject.getString("stationName"));
        JsonObject jsonValues = jsonObject.getJsonObject("values");
        ImmutableMap.Builder<PartType, Double> valuesBuilder = ImmutableMap.builder();

        for (Map.Entry<String, JsonValue> entry : jsonValues.entrySet()) {
            PartType.getByApiName(entry.getKey())
                    .ifPresent(partType ->
                            valuesBuilder.put(partType, ((JsonNumber) entry.getValue()).doubleValue()));
        }
        return new AirQualityResult(station, valuesBuilder.build());
    }

    private class AirQualityDataLoader extends CacheLoader<Long, Single<AirQualityResult>> {

        @Override
        public Single<AirQualityResult> load(Long stationId) throws Exception {
            CompletableFuture<Response> responseCompletionStage = client
                    .target("http://powietrze.gios.gov.pl/pjp/current/getAQIDetails")
                    .queryParam("id", stationId)
                    .queryParam("param", "AQI")
                    .request()
                    .rx()
                    .get().toCompletableFuture();
            return Single.fromFuture(responseCompletionStage)
                    .map((resp) -> resp.readEntity(JsonObject.class))
                    .map(AirQualityApi.this::parseResult)
                    .cache();
        }
    }
}
