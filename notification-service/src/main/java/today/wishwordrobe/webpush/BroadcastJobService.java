package today.wishwordrobe.webpush;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import today.wishwordrobe.application.PushNotificationService;
import today.wishwordrobe.presentation.dto.PushNotificationRequest;

@Service
public class BroadcastJobService {
  public enum Status{RUNNING, DONE, FAILED};

  public record JobView(
    String jobId, // 브로드캐스트 배치의 식별자, jmeter가 폴링/추적가능
    Status status,// fan-out이 진행중인지, 끝난건지, 터진건지 
    Instant createdAt, //시작시간
    Instant finishedAt,//종료시간
      long total,  //팬아웃 대상의 수 구독자수 , 토큰수
      long success, //실제 전송 성공 건수
      long failed, //전송 실패 건수
      long gone410,//webpush에서 특히 중요한 케이스 410이 gone 이면 구독이 만료 폐기됨 구독정보 삭제대상이라 따로 카운트해두는게 유용
      String error
  ){}

  private static final class JobState{
    volatile Status status=Status.RUNNING;
    final Instant createdAt=Instant.now();
    volatile Instant finishedAt;
    volatile long total,
    success, failed,gone410;
    volatile String error;
  }

  private final ConcurrentMap<String, JobState> jobs= new ConcurrentHashMap<>() ;
  private final PushNotificationService pushNotificationService;

  public BroadcastJobService(PushNotificationService pushNotificationService){
    this.pushNotificationService=pushNotificationService;
  }

  public Mono<String> enqueue(PushNotificationRequest request){
    String jobId=UUID.randomUUID().toString();
    JobState state= new JobState();
    jobs.put(jobId, state);

     pushNotificationService.broadcastAllFromDbWithStats(request)
        .publishOn(Schedulers.boundedElastic()) // job 실행을 요청 쓰레드와 분리
        .doOnSuccess(stats -> {
          state.total = stats.total();
          state.success = stats.success();
          state.failed = stats.failed();
          state.gone410 = stats.gone410();
          state.status = Status.DONE;
          state.finishedAt = Instant.now();
        })
        .doOnError(e -> {
          state.status = Status.FAILED;
          state.error = e.getMessage();
          state.finishedAt = Instant.now();
        })
        .subscribe(); // “비동기 실행”의 핵심

    return Mono.just(jobId);
  }

  public Mono<JobView> get(String jobId){
    JobState state= jobs.get(jobId);
    if(state==null) return Mono.empty();

    return Mono.just(new JobView(jobId, 
      state.status, state.createdAt,
       state.finishedAt, state.total, 
       state.success, state.failed, 
       state.gone410, state.error));
  }


}
