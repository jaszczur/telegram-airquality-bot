package pl.jaszczur.bots.aqi.aqlogic;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import org.glassfish.jersey.client.rx.RxClient;
import org.glassfish.jersey.client.rx.java8.RxCompletionStage;
import org.glassfish.jersey.client.rx.java8.RxCompletionStageInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.jaszczur.bots.aqi.commands.SetLocationCommand;

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
    private static final Logger logger = LoggerFactory.getLogger(AirQualityApi.class);

    private final RxClient<RxCompletionStageInvoker> client = RxCompletionStage.newClient();
    private final LoadingCache<Long, AirQualityResult> aqResults = CacheBuilder.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build(new AirQualityDataLoader());
    private final LoadingCache<Class, Set<Station>> stations = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build(new StationsDataLoader());

    public Single<AirQualityResult> getStats(long stationId) {
        return Single.defer(() -> {
            try {
                return Single.just(aqResults.get(stationId));
            } catch (ExecutionException ex) {
                logger.warn("error while getting station values for stationId={}", stationId, ex);
                return Single.error(ex);
            }
        });
    }

    public Single<Set<Station>> getStations() {
        return Single.defer(() -> {
            try {
                return Single.just(stations.get(AirQualityApi.class));
            } catch (ExecutionException ex) {
                logger.warn("error while getting list of stations", ex);
                return Single.error(ex);
            }
        });
    }

    public Single<Set<Station>> getStations(String name) {
        String lowerCaseName = name.toLowerCase();
        return getStations()
                .map(ss -> ss.stream().filter(s -> s.getName().toLowerCase().contains(lowerCaseName)).collect(Collectors.toSet()));
    }

    public Single<Station> getStation(long stationId) {
        return getStations()
                .map(ss -> ss.stream()
                        .filter(s -> s.getId() == stationId)
                        .findFirst()
                        .orElseThrow(() -> new NoSuchElementException("No station with id: " + stationId)));
    }

    private class AirQualityDataLoader extends CacheLoader<Long, AirQualityResult> {

        @Override
        public AirQualityResult load(Long stationId) throws Exception {
            CompletableFuture<Response> responseCompletionStage = client
                    .target("http://powietrze.gios.gov.pl/pjp/current/getAQIDetails")
                    .queryParam("id", stationId)
                    .queryParam("param", "AQI")
                    .request()
                    .rx()
                    .get().toCompletableFuture();
            return Single.fromFuture(responseCompletionStage)
                    .observeOn(Schedulers.io())
                    .map(resp -> resp.readEntity(JsonObject.class))
                    .map(this::parseResult)
                    .blockingGet();
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

    private class StationsDataLoader extends CacheLoader<Class, Set<Station>> {
        @Override
        public Set<Station> load(Class notUsed) throws Exception {
            CompletableFuture<Response> responseCompletionStage = client
                    .target("http://powietrze.gios.gov.pl/pjp/current/getAQIDetailsList")
                    .queryParam("param", "AQI")
                    .request()
                    .rx()
                    .get().toCompletableFuture();
            return Single.fromFuture(responseCompletionStage)
                    .observeOn(Schedulers.io())
                    .map((resp) -> resp.readEntity(JsonArray.class))
                    .map(this::parseResult)
                    .blockingGet();
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
