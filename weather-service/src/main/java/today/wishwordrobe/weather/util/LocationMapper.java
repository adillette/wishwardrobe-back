package today.wishwordrobe.weather.util;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * 위경도를 기반으로 측정소명과 지역코드를 매핑하는 유틸리티 클래스
 */
@Component
@Slf4j
public class LocationMapper {

    /**
     * 위경도를 기반으로 가장 가까운 측정소명을 찾습니다.
     *
     * @param longitude 경도
     * @param latitude 위도
     * @return 측정소명
     */
    public String getStationNameByCoordinates(double longitude, double latitude) {
        // 서울 주요 지역 측정소 매핑 (간단한 범위 기반)
        // 실제로는 더 정교한 거리 계산 또는 외부 API 사용 가능

        // 서울 중구 (종로구 측정소)
        if (latitude >= 37.56 && latitude <= 37.58 && longitude >= 126.97 && longitude <= 127.01) {
            log.info("위경도 ({}, {}) -> 측정소: 종로구", longitude, latitude);
            return "종로구";
        }

        // 서울 강남 (강남구 측정소)
        if (latitude >= 37.49 && latitude <= 37.52 && longitude >= 127.02 && longitude <= 127.06) {
            log.info("위경도 ({}, {}) -> 측정소: 강남구", longitude, latitude);
            return "강남구";
        }

        // 서울 송파 (송파구 측정소)
        if (latitude >= 37.50 && latitude <= 37.53 && longitude >= 127.06 && longitude <= 127.12) {
            log.info("위경도 ({}, {}) -> 측정소: 송파구", longitude, latitude);
            return "송파구";
        }

        // 서울 강동 (강동구 측정소)
        if (latitude >= 37.52 && latitude <= 37.56 && longitude >= 127.11 && longitude <= 127.16) {
            log.info("위경도 ({}, {}) -> 측정소: 강동구", longitude, latitude);
            return "강동구";
        }

        // 서울 마포 (마포구 측정소)
        if (latitude >= 37.54 && latitude <= 37.57 && longitude >= 126.90 && longitude <= 126.95) {
            log.info("위경도 ({}, {}) -> 측정소: 마포구", longitude, latitude);
            return "마포구";
        }

        // 기본값: 서울 (종로구)
        log.warn("위경도 ({}, {})에 해당하는 측정소를 찾지 못함. 기본값(종로구) 사용", longitude, latitude);
        return "종로구";
    }

    /**
     * 위경도를 기반으로 자외선 지역코드를 찾습니다.
     *
     * @param longitude 경도
     * @param latitude 위도
     * @return 자외선 지역코드 (행정구역코드)
     */
    public String getAreaNoByCoordinates(double longitude, double latitude) {
        // 서울 주요 지역코드 매핑
        // 실제로는 행정구역코드 데이터베이스 또는 API 사용 권장

        // 서울 중구/종로구
        if (latitude >= 37.56 && latitude <= 37.58 && longitude >= 126.97 && longitude <= 127.01) {
            log.info("위경도 ({}, {}) -> 지역코드: 1100000000 (서울)", longitude, latitude);
            return "1100000000";
        }

        // 서울 강남/송파/강동
        if (latitude >= 37.49 && latitude <= 37.56 && longitude >= 127.02 && longitude <= 127.16) {
            log.info("위경도 ({}, {}) -> 지역코드: 1100000000 (서울)", longitude, latitude);
            return "1100000000";
        }

        // 서울 마포/서대문
        if (latitude >= 37.54 && latitude <= 37.57 && longitude >= 126.90 && longitude <= 126.95) {
            log.info("위경도 ({}, {}) -> 지역코드: 1100000000 (서울)", longitude, latitude);
            return "1100000000";
        }

        // 부산 지역 (예시)
        if (latitude >= 35.0 && latitude <= 35.3 && longitude >= 128.9 && longitude <= 129.2) {
            log.info("위경도 ({}, {}) -> 지역코드: 2600000000 (부산)", longitude, latitude);
            return "2600000000";
        }

        // 기본값: 서울
        log.warn("위경도 ({}, {})에 해당하는 지역코드를 찾지 못함. 기본값(1100000000-서울) 사용", longitude, latitude);
        return "1100000000";
    }

    /**
     * 위경도를 기반으로 측정소명과 지역코드를 함께 반환
     *
     * @param longitude 경도
     * @param latitude 위도
     * @return LocationInfo (측정소명, 지역코드)
     */
    public LocationInfo getLocationInfo(double longitude, double latitude) {
        String stationName = getStationNameByCoordinates(longitude, latitude);
        String areaNo = getAreaNoByCoordinates(longitude, latitude);

        return new LocationInfo(stationName, areaNo);
    }

    /**
     * 측정소명과 지역코드를 담는 레코드
     */
    public record LocationInfo(String stationName, String areaNo) {}
}
