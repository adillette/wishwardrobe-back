package today.wishwordrobe.domain;

import lombok.Getter;

import java.util.Objects;
import java.time.LocalDate;

@Getter
public class WeatherCacheKey {

    private static final String PREFIX="WEATHER::";

    private  String location;
    private  LocalDate date;

    private WeatherCacheKey(String location,LocalDate date){
        if(Objects.isNull(location))
            throw new IllegalArgumentException("location can't be null");
        if(Objects.isNull(date))
            throw new IllegalArgumentException("date can't be null");
        this.location=location;
        this.date = date;
    }

    public static WeatherCacheKey from(String location,LocalDate date){
        return new WeatherCacheKey(location,date);
    }

    public static WeatherCacheKey fromString(String key){

        String[] tokens= key.split("::");
        if(tokens.length!=3)
            throw new IllegalArgumentException("Invalid key format");

        String location = String.valueOf(tokens[1]);
        LocalDate date = LocalDate.parse(tokens[2]);

        return WeatherCacheKey.from(location,date);
    }

    @Override
    public String toString() {
        return PREFIX + location +"::" + date;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        WeatherCacheKey that = (WeatherCacheKey) o;
        return Objects.equals(location, that.location) && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, date);
    }




}
