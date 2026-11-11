package today.wishwordrobe.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import today.wishwordrobe.application.UserLocationService;
import today.wishwordrobe.auth.AuthUser;
import today.wishwordrobe.presentation.dto.SendNowRequest;
import today.wishwordrobe.presentation.dto.UserLocationRequest;
import today.wishwordrobe.presentation.dto.UserLocationResponse;

@RestController
@RequestMapping("/user-location")
@RequiredArgsConstructor
public class UserLocationController {

    private final UserLocationService userLocationService;

    // 프론트가 위치 변경했을때 호출
    @PutMapping("/updatelocations")
    public Mono<ResponseEntity<UserLocationResponse>> updateLocation(
            @RequestAttribute("authUser") AuthUser authUser,
            @RequestBody UserLocationRequest request) {
        return userLocationService.updateLocation(authUser.getUserId(), request)
                .map(ResponseEntity::ok);
    }

    // 알림 버튼 클릭 시 즉시 푸시 (프론트가 현재 좌표 전달)
    @PostMapping("/send-now")
    public Mono<ResponseEntity<Void>> sendNow(
            @RequestAttribute("authUser") AuthUser authUser,
            @RequestBody SendNowRequest request) {
        return Mono
                .fromRunnable(
                        () -> userLocationService.sendNow(authUser.getUserId(), request.getLat(), request.getLon()))
                .thenReturn(ResponseEntity.accepted().<Void>build());
    }

    
    // userId 대신 본인 조회
    @GetMapping("/me") // {userId} 대신 본인 조회
    public Mono<ResponseEntity<UserLocationResponse>> getMyLocation(
        @RequestAttribute("authUser") AuthUser authUser) {
            return userLocationService.getLocationByUserId(authUser.getUserId())
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
        }
        
        // 단건 조회
        @GetMapping("/{userId}")
        public Mono<ResponseEntity<UserLocationResponse>> getLocationByUserId(
            @PathVariable("userId") long userId) {
                return userLocationService.getLocationByUserId(userId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
            }
            
    // @GetMapping("/active")
    // public Flux<UserLocationResponse> getActiveUserLocations() {
    //     return userLocationService.getActiveUserLocations();
    // }
    // 탈퇴 시 삭제
    // @DeleteMapping("/{userId}")
    // public Mono<ResponseEntity<Void>> deleteLocation(@PathVariable String userId)
    // {
    // return userLocationService.deleteByUserId(userId)
    // .thenReturn(ResponseEntity.<Void>noContent().build());
    // }
}
