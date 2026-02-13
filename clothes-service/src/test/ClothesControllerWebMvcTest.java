package today.wishwordrobe.test;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ClothesContoller.class)
class ClothesControllerWebMvcTest {
  @Autowired
  MockMvc mockMvc;

  @MockBean
  ClothesService clothesService;

  @Test
  void recommendations_returnsClothesFromService()throws Exception{
    Clothes c = Clothes.builder() 
                        .userId(1L)
                        .name("coat")
                        .category(ClothingCategory.OUTER)
                        .tempRange(TempRange.MILD)
                        .imageUrl("http://img")
                        .build();
                        c.setClothesId(10L);
                        c.setCreatedAt(LocalDateTime.of(2026,2,13,9,0));

                        //when -service mock 동작 정의
                        when(clothesService.getClothesRecommendationByLocation(1L , "Seoul", null))
                        .thenReturn(List.of(c));

                        mockMvc.perform(get("/api/clothes/recommendations")
                      .param("userId","1")
                    .param("location","Seoul"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("coat"))
                    .andExpect(jsonPath("$[0].category").value("OUTER"));

                    //verify- Service 호출 검증
                    verify(clothesService).getClothesRecommendationByLocation(1L,"Seoul",null);
                    verifyNoMoreInteractions(clothesService);
  }


  @Test
  void recommendations_withLatLon_returnsClothesFromService() throws Exception {
    //given
    Clothes c = Clothes.builder()
      .userId(1L)
      .name("jacket")
      .category(ClothingCategory.OUTER)
      .tempRange(TempRange.MILD)
      .imageUrl("http://img")
      .build();

      //when - 위도 경도 기준으로 Mock 기준 설정
       when(clothesService.getClothesRecommendationByLation(
                1L, 37.5665, 126.9780, null))
                .thenReturn(List.of(c));

        // Then
        mockMvc.perform(get("/api/clothes/recommendations")
                        .param("userId", "1")
                        .param("lat", "37.5665")   // 서울 위도
                        .param("lon", "126.9780")) // 서울 경도
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("jacket"));

        verify(clothesService).getClothesRecommendationByLatLon(
                1L, 37.5665, 126.9780, null);
  }
}
