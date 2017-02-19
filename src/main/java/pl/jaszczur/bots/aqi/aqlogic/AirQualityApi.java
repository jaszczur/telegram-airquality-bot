package pl.jaszczur.bots.aqi.aqlogic;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import io.reactivex.Single;
import org.glassfish.jersey.client.rx.RxClient;
import org.glassfish.jersey.client.rx.java8.RxCompletionStage;
import org.glassfish.jersey.client.rx.java8.RxCompletionStageInvoker;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AirQualityApi {

    private final RxClient<RxCompletionStageInvoker> client = RxCompletionStage.newClient();
    private final LoadingCache<Long, Single<AirQualityResult>> aqResults = CacheBuilder.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build(new AirQualityDataLoader());
    private final LoadingCache<Class, Single<Set<Station>>> stations = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build(new StationsDataLoader());

    public Single<AirQualityResult> getStats(long stationId) {
        return Single.defer(() -> {
            try {
                return aqResults.get(stationId);
            } catch (ExecutionException e) {
                return Single.error(e);
            }
        });
    }

    public Single<Set<Station>> getStations() {
        return Single.defer(() -> {
            try {
                return stations.get(AirQualityApi.class);
            } catch (ExecutionException e) {
                return Single.error(e);
            }
        });
    }

    public Single<Set<Station>> getStations(String name) {
        String lowerCaseName = name.toLowerCase();
        return getStations()
                .map(ss -> ss.stream().filter(s -> s.getName().toLowerCase().contains(lowerCaseName)).collect(Collectors.toSet()))
                .map(ss -> {
                    if(ss.isEmpty()) {
                        throw new NoSuchElementException("No station with name: " + name);
                    } else {
                        return ss;
                    }
                });
    }

    public Single<Station> getStation(long stationId) {
        return getStations()
                .map(ss -> ss.stream()
                        .filter(s -> s.getId() == stationId)
                        .findFirst()
                        .orElseThrow(() -> new NoSuchElementException("No station with id: " + stationId)));
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
                    .map(this::parseResult)
                    .cache();
        }

        private AirQualityResult parseResult(JsonObject jsonObject) {
            Station station = new Station(0, jsonObject.getString("stationName"));
            JsonObject jsonValues = jsonObject.getJsonObject("values");
            ImmutableMap.Builder<PartType, Double> valuesBuilder = ImmutableMap.builder();

            for (Map.Entry<String, JsonValue> entry : jsonValues.entrySet()) {
                PartType.getByApiName(entry.getKey())
                        .ifPresent(partType ->
                                valuesBuilder.put(partType, partType.getFactor() * ((JsonNumber) entry.getValue()).doubleValue()));
            }
            return new AirQualityResult(station, valuesBuilder.build());
        }
    }

    private class StationsDataLoader extends CacheLoader<Class, Single<Set<Station>>> {
        @Override
        public Single<Set<Station>> load(Class notUsed) throws Exception {
            CompletableFuture<Response> responseCompletionStage = client
                    .target("http://powietrze.gios.gov.pl/pjp/current/getAQIDetailsList")
                    .queryParam("param", "AQI")
                    .request()
                    .rx()
                    .get().toCompletableFuture();
            return Single.fromFuture(responseCompletionStage)
                    .map((resp) -> resp.readEntity(JsonArray.class))
                    .map(this::parseResult)
                    .cache();
        }

        private Set<Station> parseResult(JsonArray jsonArray) {
            return jsonArray.stream().map(item -> (JsonObject) item)
                    .map(jsonStation -> new Station(
                            jsonStation.getJsonNumber("stationId").longValue(),
                            jsonStation.getString("stationName")))
                    .collect(Collectors.toSet());
        }
    }


}
