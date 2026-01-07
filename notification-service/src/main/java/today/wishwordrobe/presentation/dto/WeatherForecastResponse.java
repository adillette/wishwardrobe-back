package today.wishwordrobe.presentation.dto;

import lombok.Data;

@Data
public class WeatherForecastResponse {
  private String region;
  private Double minTemperature;
  private Double maxTemperature;
}
