package today.wishwordrobe.weather.messaging;

import java.util.UUID;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import today.wishwordrobe.weather.application.WeatherService;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherRequestConsumer {

  private final WeatherService weatherService;
  private final WeatherEventPublisher weatherEventPublisher;

  @RabbitListener(queues = RabbitMQConfig.WEATHER_REQUEST_QUEUE)
  public void handleWeatherRequest(WeatherRequestEvent request){
     log.info("WeatherRequestEvent 수신 - userId: {}, lat: {}, lon: {}",
                request.getUserId(), request.getLat(), request.getLon());

    weatherService.getWeatherByCoordinates(request.getLat(), request.getLon())
        .doOnNext(forecast->{
          WeatherEvent event=WeatherEvent.builder()
          .eventId(UUID.randomUUID().toString())
          .userId(request.getUserId())
          .lat(request.getLat())
          .lon(request.getLon())
          .maxTemperature(forecast.getMaxTemperature())
          .minTemperature(forecast.getMinTemperature())
          .skyCondition(forecast.getSkyCondition())
          .precipitationType(forecast.getPrecipitationType())
          .build();
          weatherEventPublisher.publish(event);

        })
        .doOnError(e->log.error("날씨 조회 실패 - userId: {}", request.getUserId(), e))
        .subscribe();
      }


}
