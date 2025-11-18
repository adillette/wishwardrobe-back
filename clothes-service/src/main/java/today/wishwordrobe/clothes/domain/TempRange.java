package today.wishwordrobe.clothes.domain;

/**
 * 온도 범위 Enum
 * 온도를 범위로 분류하여 옷 추천에 활용
 */
public enum TempRange {
    VERY_COLD(-50, 4),      // 매우 추운 날씨
    COLD(5, 8),             // 추운 날씨
    COOL(9, 11),            // 쌀쌀한 날씨
    MILD(12, 16),           // 선선한 날씨
    WARM(17, 19),           // 따뜻한 날씨
    HOT(20, 22),            // 더운 날씨
    VERY_HOT(23, 27),       // 매우 더운 날씨
    EXTREME_HOT(28, 50);    // 극심하게 더운 날씨

    private final int minTemp;
    private final int maxTemp;

    TempRange(int minTemp, int maxTemp) {
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
    }

    public int getMinTemp() {
        return minTemp;
    }

    public int getMaxTemp() {
        return maxTemp;
    }

    /**
     * 온도 값을 받아서 해당하는 TempRange를 반환
     * @param temperature 온도 값
     * @return 해당 온도 범위
     */
    public static TempRange fromTemperature(int temperature) {
        for (TempRange range : values()) {
            if (temperature >= range.minTemp && temperature <= range.maxTemp) {
                return range;
            }
        }
        // 범위를 벗어나는 경우 가장 가까운 범위 반환
        if (temperature < VERY_COLD.minTemp) {
            return VERY_COLD;
        }
        return EXTREME_HOT;
    }
}
