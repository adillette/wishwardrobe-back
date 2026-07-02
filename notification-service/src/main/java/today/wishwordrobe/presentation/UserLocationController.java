package today.wishwordrobe.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import today.wishwordrobe.application.UserLocationService;
import today.wishwordrobe.infrastructure.UserLocationDocument;
import today.wishwordrobe.presentation.dto.UserLocationRequest;
import today.wishwordrobe.presentation.dto.UserLocationResponse;
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class UserLocationController {


    private final UserLocationService userLocationService;


  //프론트가 위치 변경했을때 호출
    @PutMapping("/locations")
    public Mono<ResponseEntity<UserLocationResponse>> updateLocation(@RequestBody UserLocationRequest request) {
       return userLocationService.updateLocation(request)
                   .map(ResponseEntity::ok);
    }

    @GetMapping("/active")
    public Flux<UserLocationResponse> getActiveUserLocations() {
        return userLocationService.getActiveUserLocations();
    }

    // 단건 조회
    @GetMapping("/{userId}")
    public Mono<ResponseEntity<UserLocationResponse>> getLocationByUserId(
            @PathVariable long userId) {
        return userLocationService.getLocationByUserId(userId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // 탈퇴 시 삭제
    // @DeleteMapping("/{userId}")
    // public Mono<ResponseEntity<Void>> deleteLocation(@PathVariable String userId) {
    //     return userLocationService.deleteByUserId(userId)
    //             .thenReturn(ResponseEntity.<Void>noContent().build());
    // }
}
