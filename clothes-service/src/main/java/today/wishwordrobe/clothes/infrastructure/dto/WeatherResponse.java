package today.wishwordrobe.clothes.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Weather 서비스로부터 받아오는 날씨 정보 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponse {
    private String location;
    private int temperature;
    private String description;
    private int humidity;
    private double windSpeed;
}
