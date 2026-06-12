package today.wishwordrobe.outbox.domain;

import java.time.LocalDateTime;

import org.springframework.cglib.core.Local;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document
@Getter
@Setter
@NoArgsConstructor
public class OutboxEvent {
  @Id
  private String id;

  //eventId: 멱등성 키
  //weather-service가 이벤트 발행시 UUID로 생성
  //같은 이벤트가 RabbitMQ에서 재전송 되어도 같은 eventId
  //MongoDB Unique Index가 중복 저장 차단
  @Indexed(unique = true)
  private String eventId;
  // userId: Striped Lock 키 구성 요소
  //"outbox:lock:{userId}:{date}:{eventId}" 형태로 사용
  // 사용자 단위로 락을 쪼개서 Lock Bottleneck 방지
  @Indexed
  private String userId;


  //eventType: 이벤트 종류 구분
  //Clothes_matched 등 문자열로 관리
  //OutboxProcessor 가 재처리시 어떤 처리를 해야하는지 판단에 사용
  private String eventType;

  private String payload; //실제 알림에 필요한 데이터 JSON 직렬화 저장
  //ClothesMatcedEvent 전체를 JSON으로 변환해서 저장
  //재처리시 이 payload 꺼내서 다시 FCM/WebPush 전송
  
  //현재 처리 상태 pending/ sent/ failed
  private OutboxStatus status;

   private int retryCount;// retryCount: 재시도 횟수
                              // 3회 초과 시 FAILED 마킹 → OutboxProcessor 재처리 대상 제외
   private String failureReason;// failureReason: 실패 원인 저장

   // // createdAt: 이벤트 생성 시각
    // // OutboxProcessor가 오래된 PENDING 건 찾을 때 사용
    @Indexed
    private LocalDateTime createdAt;

    // // processedAt: FCM/WebPush 전송 완료 시각
    // // cleanup 스케줄러가 7일 이상 지난 SENT 건 삭제할 때 기준
    private LocalDateTime processedAt;
  
    @Builder
    public OutboxEvent(String eventId,String userId, String eventType, String payload){
      this.eventId=eventId;
      this.userId=userId;
      this.eventType=eventType;
      this.payload=payload;
      this.status=OutboxStatus.PENDING;
      this.retryCount=0;
      this.createdAt=LocalDateTime.now();
    }


  }
