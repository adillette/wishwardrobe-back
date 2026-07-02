package today.wishwordrobe.weather.messaging;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WeatherRequestEvent {

    private long userId;
    private double lat;
    private double lon;
}
