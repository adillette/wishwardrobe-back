package today.wishwordrobe.weather.util;

import lombok.Builder;
import org.springframework.stereotype.Component;

/**
 * 기상청 단기예보 격자 좌표 변환기
 * Lambert Conformal Conic 투영법을 사용하여 위경도 ↔ 격자 좌표 변환
 * 
 * Thread-safe, Immutable
 * 
 * @author R
 */
@Component
public class WeatherGridConverter {
    
    private static final int GRID_X_MAX = 149;
    private static final int GRID_Y_MAX = 253;
    
    private final ProjectionCalculator calculator;
    
    public WeatherGridConverter() {
        // 기상청 단기예보 투영 파라미터 (고정값)
        ProjectionParams params = ProjectionParams.builder()
            .earthRadius(6371.00877)      // 지구 반경(km)
            .gridInterval(5.0)            // 격자 간격(km)
            .standardParallel1(30.0)      // 표준 위도 1
            .standardParallel2(60.0)      // 표준 위도 2
            .originLongitude(126.0)       // 기준점 경도
            .originLatitude(38.0)         // 기준점 위도
            .falseEasting(210.0 / 5.0)    // 기준점 X좌표 (42)
            .falseNorthing(675.0 / 5.0)   // 기준점 Y좌표 (135)
            .build();
        
        this.calculator = new ProjectionCalculator(params);
    }
    
    /**
     * 위경도를 격자 좌표로 변환
     * 
     * @param longitude 경도 (126.xxx)
     * @param latitude 위도 (37.xxx)
     * @return 격자 좌표 (X, Y)
     */
    public GridCoordinate toGrid(double longitude, double latitude) {
        PointXY xy = calculator.latLonToXY(latitude, longitude);
        return new GridCoordinate(
            (int)(xy.x() + 1.5),
            (int)(xy.y() + 1.5)
        );
    }
    
    /**
     * 격자 좌표를 위경도로 변환
     * 
     * @param gridX X 격자 (1 ~ 149)
     * @param gridY Y 격자 (1 ~ 253)
     * @return 위경도 좌표
     * @throws IllegalArgumentException 격자 범위 초과 시
     */
    public LatLon toLatLon(int gridX, int gridY) {
        validateGridRange(gridX, gridY);
        
        PointXY xy = new PointXY(gridX - 1, gridY - 1);
        return calculator.xyToLatLon(xy);
    }
    
    /**
     * 격자 범위 검증
     */
    private void validateGridRange(int gridX, int gridY) {
        if (gridX < 1 || gridX > GRID_X_MAX || gridY < 1 || gridY > GRID_Y_MAX) {
            throw new IllegalArgumentException(
                String.format("격자 범위 초과: X는 1~%d, Y는 1~%d 범위여야 합니다. 입력값: X=%d, Y=%d",
                    GRID_X_MAX, GRID_Y_MAX, gridX, gridY)
            );
        }
    }
    
    // ==================== DTO ====================
    
    /**
     * 격자 좌표 (X, Y)
     */
    public record GridCoordinate(int x, int y) {
        @Override
        public String toString() {
            return String.format("Grid(x=%d, y=%d)", x, y);
        }
    }
    
    /**
     * 위경도 좌표
     */
    public record LatLon(double latitude, double longitude) {
        @Override
        public String toString() {
            return String.format("LatLon(lat=%.6f, lon=%.6f)", latitude, longitude);
        }
    }
    
    /**
     * 내부 사용 XY 좌표
     */
    private record PointXY(double x, double y) {}
    
    /**
     * Lambert 투영 파라미터
     */
    @Builder
    private record ProjectionParams(
        double earthRadius,
        double gridInterval,
        double standardParallel1,
        double standardParallel2,
        double originLongitude,
        double originLatitude,
        double falseEasting,
        double falseNorthing
    ) {}
    
    // ==================== Lambert Conformal Conic 투영 계산기 ====================
    
    /**
     * Lambert Conformal Conic 투영 계산
     * 
     * Thread-safe: 모든 필드가 final이며 불변
     * Stateless: 입력값에만 의존하는 순수 함수
     */
    private static class ProjectionCalculator {
        private static final double PI = Math.PI;
        private static final double DEG_TO_RAD = PI / 180.0;
        private static final double RAD_TO_DEG = 180.0 / PI;
        private static final double EPSILON = 1e-10;
        
        // Lambert 투영 상수 (초기화 후 불변)
        private final double re;     // 정규화된 지구 반경
        private final double sn;     // 원추 상수 (cone constant)
        private final double sf;     // 축척 인수 (scale factor)
        private final double ro;     // 원점에서의 극거리 (polar distance)
        private final double olon;   // 원점 경도 (라디안)
        private final double xo;     // false easting
        private final double yo;     // false northing
        
        /**
         * 투영 파라미터로 계산기 초기화
         * 
         * @param params Lambert 투영 파라미터
         */
        public ProjectionCalculator(ProjectionParams params) {
            double slat1 = params.standardParallel1 * DEG_TO_RAD;
            double slat2 = params.standardParallel2 * DEG_TO_RAD;
            double olat = params.originLatitude * DEG_TO_RAD;
            
            this.re = params.earthRadius / params.gridInterval;
            this.olon = params.originLongitude * DEG_TO_RAD;
            this.xo = params.falseEasting;
            this.yo = params.falseNorthing;
            
            // Lambert 투영 상수 계산
            // sn = ln(cos(φ1)/cos(φ2)) / ln(tan(π/4 + φ2/2) / tan(π/4 + φ1/2))
            this.sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) /
                     Math.log(Math.tan(PI/4 + slat2/2) / Math.tan(PI/4 + slat1/2));
            
            // sf = cos(φ1) × tan^n(π/4 + φ1/2) / n
            this.sf = Math.pow(Math.tan(PI/4 + slat1/2), sn) * Math.cos(slat1) / sn;
            
            // ro = r × F / tan^n(π/4 + φ0/2)
            this.ro = re * sf / Math.pow(Math.tan(PI/4 + olat/2), sn);
        }
        
        /**
         * 위경도를 XY 좌표로 변환
         * 
         * @param lat 위도 (degree)
         * @param lon 경도 (degree)
         * @return XY 좌표
         */
        public PointXY latLonToXY(double lat, double lon) {
            // ρ = r × F / tan^n(π/4 + φ/2)
            double ra = re * sf / Math.pow(Math.tan(PI/4 + lat * DEG_TO_RAD / 2), sn);
            
            // θ = n × (λ - λ0)
            double theta = normalizeAngle((lon * DEG_TO_RAD - olon) * sn);
            
            // x = ρ × sin(θ) + x0
            // y = ρ0 - ρ × cos(θ) + y0
            double x = ra * Math.sin(theta) + xo;
            double y = ro - ra * Math.cos(theta) + yo;
            
            return new PointXY(x, y);
        }
        
        /**
         * XY 좌표를 위경도로 변환
         * 
         * @param point XY 좌표
         * @return 위경도
         */
        public LatLon xyToLatLon(PointXY point) {
            double xn = point.x - xo;
            double yn = ro - point.y + yo;
            
            // ρ = √(x'^2 + y'^2)
            double ra = Math.sqrt(xn * xn + yn * yn);
            if (sn < 0.0) ra = -ra;
            
            // φ = 2 × arctan((r×F/ρ)^(1/n)) - π/2
            double alat = 2.0 * Math.atan(Math.pow(re * sf / ra, 1.0 / sn)) - PI / 2;
            
            // θ = arctan(x'/y')
            double theta = calculateTheta(xn, yn);
            
            // λ = θ/n + λ0
            double alon = theta / sn + olon;
            
            return new LatLon(alat * RAD_TO_DEG, alon * RAD_TO_DEG);
        }
        
        /**
         * θ 각도 계산 (특수 케이스 처리)
         */
        private double calculateTheta(double xn, double yn) {
            if (Math.abs(xn) <= EPSILON) {
                return 0.0;
            } else if (Math.abs(yn) <= EPSILON) {
                return (xn < 0.0) ? -PI/2 : PI/2;
            } else {
                return Math.atan2(xn, yn);
            }
        }
        
        /**
         * 각도를 [-π, π] 범위로 정규화
         */
        private double normalizeAngle(double angle) {
            while (angle > PI) angle -= 2.0 * PI;
            while (angle < -PI) angle += 2.0 * PI;
            return angle;
        }
    }
}
