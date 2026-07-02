package today.wishwordrobe.outbox.application;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockService {
  private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

  //락 TTL 30초
  //FCM 전송 최대 소요 시간 고려 여유분
  //서버가 락 잡고 죽어도 30초 후 자동해제 deadlock 방지
  private static final Duration LOCK_TTL = Duration.ofSeconds(30);

  private static final String LOCK_PREFIX="outbox:lock";

  //buildLockKey: Striped Lock 효과를 위한 키설계
  /*기존 단순 eventId 키의 문제 모든 이벤트가 각자 락을 가지지만 userId 구분 없이 락 경합 발생가능

  -> userId+ date + eventId 조합
  userid 단위로 락이 분산 
  Lock Bottleneck 원천 차단
  같은 userId의 당일 중복 요청만 선택적으로 차단

   */

  private String buildLockKey(Long userId, String eventId){
    String today=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyMMdd"));
    return LOCK_PREFIX+String.valueOf(userId)+ ":"+ today+":"+eventId;
  }

  //trylock: redis set nx ex(atomic 명령어)
  //SET NX= NOT EXISTS: 키 없을때만 저장
  //EX -TTL 설정
  //이 명령어 자체가 atomic -> race condition 없음
  //true: 락 획득 이 인스턴스가 독점 처리
  //false: 같은 userId의 동일 이벤트 처리중-> skip
          //다른 userId는 완전히 별개 키 -> 방해 없음

  //락 해제 원자성
  //내가 건 락인지 확인하고 삭제한다.
  private static final RedisScript<Boolean> RELEASE_SCRIPT= RedisScript.of(
    " if redis.call('get',KEYS[1]) == ARGV[1] then "+
    " return redis.call('del', KEYS[1]) "+
    " else return 0 end",
    Boolean.class);

  public Mono<String> tryLock(Long userId, String eventId){
    String lockKey = buildLockKey(userId, eventId);
    String lockValue= UUID.randomUUID().toString();// 소유권 식별자

    return reactiveRedisTemplate.opsForValue()
              .setIfAbsent(lockKey, lockValue, LOCK_TTL)
              .map(acquired-> Boolean.TRUE.equals(acquired)? lockValue: null)
              .doOnNext(val-> { 
                if (val != null) log.debug("락 획득. userId={}, eventId={}", userId, eventId);
                else log.warn("락 획득 실패 - 중복 차단. userId={}, eventId={}", userId, eventId);
              }); 
  }

  public Mono<Boolean> releaseLock(Long userId, String eventId, String lockValue){
    String lockKey = buildLockKey(userId, eventId);
     return reactiveRedisTemplate.execute(
            RELEASE_SCRIPT,
            List.of(lockKey),
            List.of(lockValue)
    )
    .next()
    .defaultIfEmpty(false)
    .doOnNext(released ->
            log.debug("분산락 해제. userId={}, eventId={}, released={}", userId, eventId, released)
        );
   }
}
