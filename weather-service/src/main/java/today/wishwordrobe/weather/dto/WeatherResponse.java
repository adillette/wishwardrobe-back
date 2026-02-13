package today.wishwordrobe.weather.dto;

import lombok.Data;

@Data
public class WeatherResponse {
  private String location;
    private int temperature;      // 평균 온도 (최고/최저 평균)
    private String description;
    private int humidity;
    private double windSpeed;

}
