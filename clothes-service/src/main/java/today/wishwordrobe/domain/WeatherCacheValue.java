package today.wishwordrobe.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@ToString
@EqualsAndHashCode
public class WeatherCacheValue {

    private final Double maxTemperature;
    private final Double minTemperature;
    private final Integer humidity;
    private final String skyCondition;
    private final String precipitationType;
    private final LocalDate cacheDate;

    public WeatherCacheValue(Double maxTemperature, Double minTemperature,
                             Integer humidity, String skyCondition,
                             String precipitationType, LocalDate cacheDate) {
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
        this.humidity = humidity;
        this.skyCondition = skyCondition;
        this.precipitationType = precipitationType;
        this.cacheDate = cacheDate;
    }
}
