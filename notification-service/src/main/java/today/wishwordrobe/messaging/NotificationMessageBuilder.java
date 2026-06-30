package today.wishwordrobe.messaging;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

//알림 메시지 제목이랑 본문 생성 전담 클래스
//신규 전송(NotificationConsumer)와 재처리(OutboxProcessor) 둘다 여기서 가져다씀
//메시지 형식 변경시 이 파일 수정
@Component
public class NotificationMessageBuilder {
  //알림 제목 생성
  //날씨 상태 + 강수 형태 조합
  public String buildTitle(ClothesMatchedEvent event){
    String sky= Optional.ofNullable(event.getSkyCondition()).orElse("기상 예측정보가 없습니다.");
    String precip = Optional.ofNullable(event.getPrecipitationType())
    .filter(p->!p.equals("없음"))
    .orElse("맑음");

    return String.format("오늘의 날씨 %s | %s",sky,precip);
  }

  //알림 본문 생성
  //최고 최저온도 +추천 옷 목록 3개

  public String buildBody(ClothesMatchedEvent event){
    String tempInfo="";
    if(event.getMaxTemperature()!=null && event.getMinTemperature()!=null){
      tempInfo=String.format("최고 %.0f°C / 최저 %.0f°C\n",
                    event.getMaxTemperature(),
                    event.getMinTemperature());
    }
    String clothesInfo="";

    if(event.getRecommendedClothes()!=null&& !event.getRecommendedClothes().isEmpty()){
      clothesInfo="추천 옷: "+ event.getRecommendedClothes().stream()
      .map(e->e.getName())
      .limit(3)
      .collect(Collectors.joining(", "));
    }
    return tempInfo+clothesInfo;
  }
}
