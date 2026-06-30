package today.wishwordrobe.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationMessageBuilderTest {

    private NotificationMessageBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new NotificationMessageBuilder();
    }

    // ───── buildTitle ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("buildTitle")
    class BuildTitle {

        @Test
        @DisplayName("맑음 + 강수 없음 → 기본 표시")
        void clear_no_precip() {
            ClothesMatchedEvent event = event("맑음", "없음", null, null, List.of());
            assertThat(builder.buildTitle(event)).isEqualTo("오늘의 날씨 맑음 | 맑음");
        }

        @Test
        @DisplayName("구름 + 비 → 강수 형태 표시")
        void cloudy_rain() {
            ClothesMatchedEvent event = event("구름많음", "비", null, null, List.of());
            assertThat(builder.buildTitle(event)).isEqualTo("오늘의 날씨 구름많음 | 비");
        }

        @Test
        @DisplayName("skyCondition null → 기본 메시지 표시")
        void null_sky() {
            ClothesMatchedEvent event = event(null, "없음", null, null, List.of());
            assertThat(builder.buildTitle(event)).contains("기상 예측정보가 없습니다.");
        }

        @Test
        @DisplayName("precipitationType null → 맑음 표시")
        void null_precip() {
            ClothesMatchedEvent event = event("맑음", null, null, null, List.of());
            assertThat(builder.buildTitle(event)).isEqualTo("오늘의 날씨 맑음 | 맑음");
        }
    }

    // ───── buildBody ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("buildBody")
    class BuildBody {

        @Test
        @DisplayName("온도 + 옷 3개 → 온도와 옷 이름 포함")
        void temperature_and_three_clothes() {
            List<ClothesItemDto> clothes = clothes("반팔", "청바지", "운동화");
            ClothesMatchedEvent event = event("맑음", "없음", 28.0, 18.0, clothes);

            String body = builder.buildBody(event);

            assertThat(body).contains("28°C", "18°C", "반팔", "청바지", "운동화");
        }

        @Test
        @DisplayName("옷이 3개 초과여도 3개만 포함")
        void clothes_limit_3() {
            List<ClothesItemDto> clothes = clothes("반팔", "청바지", "운동화", "모자", "가디건");
            ClothesMatchedEvent event = event("맑음", "없음", 25.0, 15.0, clothes);

            String body = builder.buildBody(event);

            assertThat(body).contains("반팔", "청바지", "운동화");
            assertThat(body).doesNotContain("모자", "가디건");
        }

        @Test
        @DisplayName("온도 null → 온도 정보 없음")
        void null_temperature() {
            List<ClothesItemDto> clothes = clothes("패딩");
            ClothesMatchedEvent event = event("흐림", "없음", null, null, clothes);

            String body = builder.buildBody(event);

            assertThat(body).doesNotContain("°C");
            assertThat(body).contains("패딩");
        }

        @Test
        @DisplayName("추천 옷 없음 → 옷 정보 없음")
        void no_clothes() {
            ClothesMatchedEvent event = event("맑음", "없음", 25.0, 15.0, List.of());

            String body = builder.buildBody(event);

            assertThat(body).contains("25°C", "15°C");
            assertThat(body).doesNotContain("추천 옷");
        }

        @Test
        @DisplayName("추천 옷 null → 옷 정보 없음")
        void null_clothes() {
            ClothesMatchedEvent event = event("맑음", "없음", 25.0, 15.0, null);

            String body = builder.buildBody(event);

            assertThat(body).doesNotContain("추천 옷");
        }
    }

    // ───── helpers ────────────────────────────────────────────────────────────

    private ClothesMatchedEvent event(String sky, String precip,
                                      Double maxTemp, Double minTemp,
                                      List<ClothesItemDto> clothes) {
        ClothesMatchedEvent e = new ClothesMatchedEvent();
        e.setSkyCondition(sky);
        e.setPrecipitationType(precip);
        e.setMaxTemperature(maxTemp);
        e.setMinTemperature(minTemp);
        e.setRecommendedClothes(clothes);
        return e;
    }

    private List<ClothesItemDto> clothes(String... names) {
        return java.util.Arrays.stream(names).map(name -> {
            ClothesItemDto dto = new ClothesItemDto();
            dto.setName(name);
            return dto;
        }).toList();
    }
}
