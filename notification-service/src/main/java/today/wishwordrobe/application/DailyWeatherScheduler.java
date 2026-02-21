package today.wishwordrobe.application;

import java.util.HashMap;
import java.util.Map;

import org.apache.hc.client5.http.auth.AuthStateCacheable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import today.wishwordrobe.firebase.FCMPushNotificationRequest;
import today.wishwordrobe.firebase.FCMService;
import today.wishwordrobe.presentation.dto.ClothesDto;
import today.wishwordrobe.presentation.dto.PushNotificationRequest;
import today.wishwordrobe.presentation.dto.WeatherForecastResponse;
import today.wishwordrobe.webpush.BroadcastJobService;
import today.wishwordrobe.webpush.WebPushService;

@Component
@Slf4j
public class DailyWeatherScheduler {

  @Autowired
  @Qualifier("weatherServiceWebClient")
  private WebClient weatherServiceWebClient;

  @Autowired
  @Qualifier("clothesServiceWebClient")
  private WebClient clothesServiceWebClient;

  @Autowired
  private FCMService fcmService;

  @Autowired
  private PushNotificationService pushNotificationService;

  @Autowired
  private WebPushService webPushService;

  @Autowired
  private BroadcastJobService broadcastJobService;

  @Value("${notification.daily.enabled:true}")
  private boolean enabled;

  @Value("${notification.daily.lat}")
  private double lat;

  @Value("${notification.daily.lon}")
  private double lon;

  @Value("${notification.daily.location-label:}")
  private String locationLabel;

  @Value("${notification.daily.mobile-topic:daily-weather}")
  private String mobileTopic;

  @Value("${notification.daily.mobile-title:오늘의 날씨}")
  private String mobileTitle;

  @Value("${notification.daily.web-title:오늘의 날씨}")
  private String webTitle;

  @Value("${notification.daily.message:}")
  private String defaultMessage;


  //대표아이디로 먼저 테스트
   @Value("${notification.daily.default-user-id:1}")  // ← 추가
  private Long defaultUserId;
  @Value("${notification.daily.click-action:https://wishwordrobe.today}")
  private String clickAction;

  @Scheduled(cron = "${notification.daily.cron:0 0 7 * * *}", zone = "${notification.daily.zone:Asia/Seoul}")
  public void run() {
    if (!enabled)
      return;

    fetchForecast(lat, lon)
        .flatMap(forecast -> {
          String label = (locationLabel == null || locationLabel.isBlank())
              ? String.format("%.4f,%.4f", lat, lon)
              : locationLabel;

          String message = buildMessage(label, forecast);
          String mobileMessage = (defaultMessage != null && !defaultMessage.isBlank())
              ? defaultMessage + " / " + message
              : message;

          Mono<String> mobile = sendMobileFcm(mobileTitle, mobileMessage);
          Mono<Map<String, Object>> web = sendWebPushBroadcast(webTitle, message);

          return Mono.whenDelayError(mobile, web);
        })
        .doOnSuccess(v -> log.info("07:00 weather push done"))
        .doOnError(e -> log.error("07:00 weather push failed", e))
        .subscribe();
  }

  private Mono<WeatherForecastResponse> fetchForecast(double lat, double lon) {
    return weatherServiceWebClient.get()
        .uri(uri -> uri.path("/weather/coordinates")
            .queryParam("lat", lat)
            .queryParam("lon", lon)
            .build())
        .retrieve()
        .bodyToMono(WeatherForecastResponse.class);
  }

  // 최저(min) 기준 추천
  private String buildMessage(String locationLabel, WeatherForecastResponse forecast) {
    int min = forecast.getMinTemperature() == null ? 0 : (int) Math.round(forecast.getMinTemperature());
    int max = forecast.getMaxTemperature() == null ? 0 : (int) Math.round(forecast.getMaxTemperature());

    String clothes = recommend(min); // avg 대신 min!
    return String.format("%s %d~%d°C · 최저 %d°C 기준 추천: %s", locationLabel, min, max, min, clothes);
  }

  private String recommend(int temperature) {
    if (temperature >= 28)
      return "민소매/반팔/반바지";
    if (temperature >= 23)
      return "반팔/얇은 셔츠";
    if (temperature >= 20)
      return "얇은 가디건/긴팔";
    if (temperature >= 17)
      return "얇은 니트/맨투맨";
    if (temperature >= 12)
      return "자켓/가디건";
    if (temperature >= 9)
      return "자켓/트렌치/니트";
    if (temperature >= 5)
      return "코트/히트텍";
    return "패딩/두꺼운 코트";
  }

  private Mono<String> sendMobileFcm(String title, String message) {
    FCMPushNotificationRequest request = new FCMPushNotificationRequest(
        title,
        message,
        null,
        null,
        Map.of("type", "weather", "lat", String.valueOf(lat), "lon", String.valueOf(lon)),
        null,
        mobileTopic,
        null);

    return fcmService.sendTopicMessage(request)
        .doOnSuccess(id -> log.info("FCM sent: {}", id))
        .doOnError(e -> log.error("FCM failed", e));
  }

  private Mono<Map<String, Object>> sendWebPushBroadcast(String title, 
    String message) {

      return fetchRecommendedClothesImage().flatMap(imageUrl->{
         log.info("Fetched recommended clothes image: {}", imageUrl);
         PushNotificationRequest request =PushNotificationRequest.builder()
                                            .title(title)
                                            .message(message)
                                            .icon(null)
                                            .clickAction(clickAction)
                                            .data(Map.of("type", "weather", "lat", String.valueOf(lat), "lon", String.valueOf(lon)))
                                            .url(null)
                                            .image(imageUrl)
                                            .build();
      return broadcastJobService.enqueue(request)
      .map(jobId -> {
        Map<String, Object> result = new HashMap<>();
        result.put("jobId", jobId);
        result.put("type", "webpush");
        return result;
      })
      .doOnSuccess(result -> log.info("WebPush broadcast job queued: {}", result))
      .doOnError(e -> log.error("WebPush broadcast job enqueue failed", e));
  });
}
      //clothes-service 에서 추천 옷 이미지 url 가져오기
 
  private Mono<String> fetchRecommendedClothesImage() {
    return clothesServiceWebClient.get()
        .uri(uri -> uri.path("/recommendations")
            .queryParam("userId", defaultUserId)
            .queryParam("lat", lat)
            .queryParam("lon", lon)
            .build())
        .retrieve()
        .bodyToFlux(ClothesDto.class)
        .next()  // 첫 번째만
        .map(ClothesDto::getImageUrl)
        .doOnNext(url -> log.info("Clothes recommendation image URL: {}", url))
        .onErrorResume(e -> {
          log.warn("Failed to fetch clothes recommendation, using no image. Error: {}", e.getMessage());
          return Mono.just((String) null);  // 실패 시 null로 fallback
        });
  }

  @Scheduled(cron = "0 0 0 * * *") // 매일 오전 12시 (자정)
    public void cleanupExpiredSubscriptions() {
        log.info("[배치] WebPush 만료 구독 정리 시작");
        webPushService.deleteInactiveSubscriptions()
                .subscribe(
                    count -> log.info("[배치] WebPush 만료 구독 {}건 삭제", count),
                    e    -> log.error("[배치] WebPush 만료 구독 정리 실패", e)
                );
    }

}