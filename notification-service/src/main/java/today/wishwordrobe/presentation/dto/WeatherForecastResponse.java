package today.wishwordrobe.presentation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeatherForecastResponse {
  private String region;
  private Double minTemperature;
  private Double maxTemperature;
}
