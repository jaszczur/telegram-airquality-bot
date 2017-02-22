package pl.jaszczur.bots.aqi.aqlogic;

import com.google.common.cache.Cache;
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
    private final AirQualityDataLoader airQualityDataLoader = new AirQualityDataLoader(client);
    private final StationsDataLoader stations = new StationsDataLoader(client);

    public Single<AirQualityResult> getStats(long stationId) {
        return airQualityDataLoader.get(stationId)
                .doOnError(err -> {
                    logger.warn("error while getting station values for stationId={}", stationId, err);
                });
    }

    public Single<Set<Station>> getStations() {
        return stations.get()
                .doOnError(err -> {
            logger.warn("error while getting list of stations", err);
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
}

class StationsDataLoader {
    private final RxClient<RxCompletionStageInvoker> client;
    private final Cache<Class, Set<Station>> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build();

    StationsDataLoader(RxClient<RxCompletionStageInvoker> client) {
        this.client = client;
    }

    Single<Set<Station>> get() {
        return Single.defer(() -> {
            Set<Station> cachedResult = cache.getIfPresent(StationsDataLoader.class);
            if (cachedResult == null) {
                return loadAndCache();
            } else {
                return Single.just(cachedResult);
            }

        });
    }

    private Single<Set<Station>> loadAndCache() {
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
                .doOnSuccess(value -> cache.put(StationsDataLoader.class, value));
    }

    private Set<Station> parseResult(JsonArray jsonArray) {
        return jsonArray.stream().map(item -> (JsonObject) item)
                .map(jsonStation -> new Station(
                        jsonStation.getJsonNumber("stationId").longValue(),
                        jsonStation.getString("stationName")))
                .collect(Collectors.toSet());
    }
}

class AirQualityDataLoader {
    private final RxClient<RxCompletionStageInvoker> client;
    private final Cache<Long, AirQualityResult> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build();

    AirQualityDataLoader(RxClient<RxCompletionStageInvoker> client) {
        this.client = client;
    }

    Single<AirQualityResult> get(long stationId) {
        return Single.defer(() -> {
            AirQualityResult cachedResult = cache.getIfPresent(stationId);
            if (cachedResult == null) {
                return loadAndCache(stationId);
            } else {
                return Single.just(cachedResult);
            }

        });
    }

    private Single<AirQualityResult> loadAndCache(long stationId) {
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
                .doOnSuccess(value -> cache.put(stationId, value));
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