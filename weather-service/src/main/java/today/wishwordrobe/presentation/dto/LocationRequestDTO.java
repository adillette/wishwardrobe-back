package today.wishwordrobe.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationRequestDTO {
    private String region3depthName; // 동 단위 정보
    private Double longitude;        // 경도 (x)
    private Double latitude;         // 위도 (y)
}
