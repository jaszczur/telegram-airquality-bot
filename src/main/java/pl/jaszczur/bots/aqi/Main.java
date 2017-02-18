package pl.jaszczur.bots.aqi;

import com.google.common.collect.ImmutableMap;
import org.glassfish.jersey.client.rx.RxClient;
import org.glassfish.jersey.client.rx.rxjava.RxObservable;
import org.glassfish.jersey.client.rx.rxjava.RxObservableInvoker;
import rx.Observable;
import rx.schedulers.Schedulers;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.core.Response;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        System.out.println("Odpytujemy serwera");
        RxClient<RxObservableInvoker> client = RxObservable.newClient();
        Observable<Response> observable = client
                .target("http://powietrze.gios.gov.pl/pjp/current/getAQIDetails")
                .queryParam("id", 117)
                .queryParam("param", "AQI")
                .request()
                .rx()
                .get();
        observable
                .map((resp) -> resp.readEntity(JsonObject.class))
                .map(Main::parseResult)
                .subscribeOn(Schedulers.immediate())
                .subscribe(System.out::println, Throwable::printStackTrace, client::close);
    }

    private static AirQualityResult parseResult(JsonObject jsonObject) {
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
