package today.wishwordrobe.application;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import today.wishwordrobe.auth.AuthUser;
import today.wishwordrobe.infrastructure.UserLocationDocument;
import today.wishwordrobe.infrastructure.UserLocationRepository;
import today.wishwordrobe.messaging.WeatherRequestEvent;
import today.wishwordrobe.messaging.WeatherRequestPublisher;
import today.wishwordrobe.presentation.dto.UserLocationRequest;
import today.wishwordrobe.presentation.dto.UserLocationResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserLocationService {
    private final UserLocationRepository userLocationRepository;
    private final WeatherRequestPublisher weatherRequestPublisher;

    // 알림 버튼 클릭 시 7시 스케줄러와 동일한 파이프라인으로 즉시 발송
    public void sendNow(Long userId, double lat, double lon){
      weatherRequestPublisher.publish(
        WeatherRequestEvent.builder()
            .userId(userId)
            .lat(lat)
            .lon(lon)
            .build());
      log.info("알림 즉시 발송 요청 - userId: {}", userId);
    }

    public Flux<UserLocationResponse> getActiveUserLocations(){
      return userLocationRepository.findByEnabled(true)
              .map(this::toResponse);
    }

    public Mono<UserLocationResponse> updateLocation(Long userId, UserLocationRequest request){
      return userLocationRepository.findByUserId(userId)
              .flatMap(existing->{
                existing.setLat(request.getLat());
                existing.setLon(request.getLon());
                existing.setEnabled(request.isEnabled());
                existing.setUpdatedAt(LocalDateTime.now());
                return userLocationRepository.save(existing);
              })
              .switchIfEmpty(
                userLocationRepository.save(
                  UserLocationDocument.builder()
                            .userId(userId)
                            .lat(request.getLat())
                            .lon(request.getLon())
                            .enabled(request.isEnabled())
                            .updatedAt(LocalDateTime.now())
                            .build()
                          )
                        ).map(this::toResponse);
      
    }

    public Mono<UserLocationResponse> getLocationByUserId(long userId){
      return userLocationRepository.findByUserId(userId)
              .map(this::toResponse);
    }


    private UserLocationResponse toResponse(UserLocationDocument doc) {
        return UserLocationResponse.builder()
                .userId(doc.getUserId())
                .lat(doc.getLat())
                .lon(doc.getLon())
                .enabled(doc.isEnabled())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }



}
