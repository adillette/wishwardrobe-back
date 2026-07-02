package today.wishwordrobe.application;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import today.wishwordrobe.infrastructure.UserLocationDocument;
import today.wishwordrobe.infrastructure.UserLocationRepository;
import today.wishwordrobe.presentation.dto.UserLocationRequest;
import today.wishwordrobe.presentation.dto.UserLocationResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserLocationService {
    private final UserLocationRepository userLocationRepository;

    public Flux<UserLocationResponse> getActiveUserLocations(){
      return userLocationRepository.findByEnabled(true)
              .map(this::toResponse);
    }

    public Mono<UserLocationResponse> updateLocation(UserLocationRequest request){
      return userLocationRepository.findById(String.valueOf(request.getUserId()))
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
                            .userId(request.getUserId())
                            .lat(request.getLat())
                            .lon(request.getLon())
                            .enabled(request.isEnabled())
                            .build()
                          )
                        ).map(this::toResponse);
      
    }

    public Mono<UserLocationResponse> getLocationByUserId(long userId){
      return userLocationRepository.findById(String.valueOf(userId))
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
