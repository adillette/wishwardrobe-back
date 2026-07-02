package today.wishwordrobe.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherRequestEvent {
    private long userId;  // String → long
    private double lat;
    private double lon;
}
