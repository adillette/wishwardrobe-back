package today.wishwordrobe.webpush;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.asynchttpclient.AsyncHttpClient;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Encoding;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushAsyncService;
import reactor.core.publisher.Mono;
import today.wishwordrobe.infrastructure.WebPushSubscriptionRepository;
import today.wishwordrobe.presentation.dto.WebPushSubscriptionDocument;

@Service
@Slf4j
public class WebPushService {

    private final AsyncHttpClient asyncHttpClient;

    private final PushAsyncService pushAsyncService;

    private final WebPushSubscriptionRepository webPushSubscriptionRepository;

    public enum SendResult { SUCCESS, EXPIRED, FAILED }

    private static final int MAX_DEVICES_PER_USER = 3;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public WebPushService(
            WebPushSubscriptionRepository 
            webPushSubscriptionRepository, 
            PushAsyncService pushAsyncService, 
            AsyncHttpClient asyncHttpClient) {
        
        this.webPushSubscriptionRepository = webPushSubscriptionRepository;
        this.pushAsyncService = pushAsyncService;
        this.asyncHttpClient = asyncHttpClient;
    }

    public Mono<WebPushSubscriptionDocument> saveWebPushSubscription(String userId, WebPushSubscription subscription) {
        String endpoint = subscription.getEndpoint();
        //엔드 포인트를 구독의 고유 식별자로 사용
        return webPushSubscriptionRepository.findById(endpoint)
        //db에서 해당 endpoint 가 이미 존재하는지 조회
                .flatMap(existing -> {
                    //조회결과가 기존문서에 있을때만 실행되는데 existing = db에서 꺼낸 webpushsubscriptiondocument 객체
                    existing.setUserId(userId);//구독자 userId 갱신
                    existing.setLastUsedAt(LocalDateTime.now());//마지막 사용시간 갱신
                    existing.setActive(true);//비활성화됐을 경우를 대비해서 다시 확성화
                    log.info("webpush 재구독 갱신: userId={}, endpoint={}", userId, endpoint);
                    return webPushSubscriptionRepository.save(existing);//수정된 기존 문서를 db에 저장
                })
                .switchIfEmpty(// 신규 구독
                        enforceWebPushDeviceLimit(userId)
                        //유저의 디바이스 등록 수 제한체크
                        //초과시 mono가 error를 emit하거나 오래된 것을 제거한다.
                                .then(Mono.defer(() -> {
                                    //enforceWebPushDeviceLimit완료 후 실행 보장
                                    //defer: 실제 구독 시점까지 내부 코드 생성을 지연시킴
                                    //defer없으면 doc 객체가 limit 체크전에 만들어진다. 
                                    WebPushSubscriptionDocument doc = WebPushSubscriptionDocument.from(subscription,
                                            userId);
                                    //subscription dto-> Mongodb 문서 객체로 변환
                                    log.info("WebPush 신규 구독 저장: userId={}, endpoint={}", userId, endpoint);
                                    return webPushSubscriptionRepository.save(doc);
                                    //새문서 db 저장
                                })))
                .doOnSuccess(saved -> log.info("WebPush 구독 저장 완료: userId={}, endpoint={}", userId, endpoint));
    }

    private Mono<Void> enforceWebPushDeviceLimit(String userId) {
        return webPushSubscriptionRepository
                .findByUserIdAndIsActiveOrderByLastUsedAtAsc(userId, true)
                .skip(MAX_DEVICES_PER_USER)
                .flatMap(oldest -> {
                    log.warn("WebPush 디바이스 제한 초과 - 삭제: userId={}, endpoint={}",
                            userId, oldest.getEndpoint());
                    return webPushSubscriptionRepository.delete(oldest);
                })
                .then();
    }
    // Document를 직접 받아서 410/404 내부에서 처리
    //preparePost: 알림 내용을 Http요청 형태로 변환
    //asyncHttpClient.executeRequest: 실제 Http 요청전송
    //toCompletableFuture: mono로 변환하려고
    /* Lombok @Builder - 우리가 만든 것
FCMPushNotificationRequest.builder()
    .token("abc")
    .build(); // // FCMPushNotificationRequest 완성

// // BoundRequestBuilder - 라이브러리가 만든 것
pushAsyncService.preparePost(notification, Encoding.AES128GCM)
// // 이미 BoundRequestBuilder 상태로 반환됨 - .builder() 호출 없음
    .build(); // // Request 완성
*/
   public Mono<SendResult> sendNotification(WebPushSubscriptionDocument doc, 
WebPushNotificationRequest request) {

    

    return Mono.fromCallable(() ->new Notification(
            doc.getEndpoint(),
            doc.getP256dh(),
            doc.getAuth(),
            createPayload(request).getBytes(StandardCharsets.UTF_8)))
        // ↑ NoSuchAlgorithmException → 자동으로 Mono.error()로 변환
            .flatMap(notification -> 
                Mono.fromCallable(()->
            pushAsyncService.preparePost(notification, Encoding.AES128GCM).build())
            )
            // ↑ JoseException, GeneralSecurityException 등도 자동 변환
            // // checked exception → 자동으로 Mono.error()로 변환
            
            .flatMap(post -> Mono.fromFuture(
                asyncHttpClient.executeRequest(post).toCompletableFuture()
            ))
            .map(response -> response.getStatusCode())
            .flatMap(status -> {
                if (status == 410 || status == 404) {
                    log.warn("WebPush {} - 만료 구독 삭제: endpoint={}", status, 
                    doc.getEndpoint());
                    return webPushSubscriptionRepository.deleteById(doc.getEndpoint())
                            .thenReturn(SendResult.EXPIRED);
                }
                if (status >= 200 && status < 300) {
                    log.debug("WebPush 전송 성공: status={}, endpoint={}", status, 
                    doc.getEndpoint());
                    doc.setLastUsedAt(LocalDateTime.now());
                    return webPushSubscriptionRepository.save(doc).thenReturn(
                    SendResult.SUCCESS);
                }
                log.error("WebPush 전송 실패: status={}, endpoint={}", status, 
                doc.getEndpoint());
                return Mono.error(new WebPushSendException(doc.getEndpoint(), 
                status, null));
            });
}


    //구독 삭제 
    public Mono<Void> deleteByEndpoint(String endpoint){
        return webPushSubscriptionRepository.deleteById(endpoint)
        .doOnSuccess(v->log.info("webPush 구독 해제: endpoint={}",endpoint));

    }
    //구독 전체 삭제
    public Mono<Void> deleteByUserId(String userId){
        return webPushSubscriptionRepository.deleteByUserId(userId)
        .doOnSuccess(v->log.info("WebPush 구독 전체 삭제: userId={}",userId));
    }

    //7일 미사용 구독 정리
    public Mono<Long> deleteInactiveSubscriptions(){
        LocalDateTime threshod= LocalDateTime.now().minusDays(7);
        return webPushSubscriptionRepository.findByLastUsedAtBeforeAndIsActive(threshod, true)
        .flatMap(doc->webPushSubscriptionRepository.delete(doc).thenReturn(1L))
        .reduce(0L,Long::sum)
        .doOnSuccess(count->log.info("WebPush 만료 구독 {}건 삭제 완료", count));
    }





    private String createPayload(WebPushNotificationRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", request.getTitle());
        payload.put("message", request.getMessage());
        payload.put("icon", request.getIcon());
        payload.put("clickAction", request.getClickAction());
        payload.put("data", request.getData());
        payload.put("url", request.getUrl());
        payload.put("image", request.getImage());

        try {
            return OBJECT_MAPPER.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error creating web push payload JSON", e);
        }
    }
}