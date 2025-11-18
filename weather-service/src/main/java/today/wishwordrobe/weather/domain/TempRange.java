package today.wishwordrobe.weather.domain;

public enum TempRange {
    VERY_HOT(28, 50),    // 민소매, 반팔, 반바지, 원피스
    HOT(23, 27),         // 반팔, 얇은 셔츠, 반바지, 면바지
    WARM(20, 22),        // 얇은 가디건, 긴팔, 면바지, 청바지
    MILD(17, 19),        // 얇은 니트, 맨투맨, 가디건, 청바지
    COOL(12, 16),        // 자켓, 가디건, 야상, 스타킹, 청바지, 면바지
    CHILLY(9, 11),       // 자켓, 트렌치코트, 야상, 니트, 청바지, 스타킹
    COLD(5, 8),          // 코트, 가죽자켓, 히트텍, 니트, 레깅스
    VERY_COLD(-15, 4);
    private final int minTemp;
    private final int maxTemp;

    TempRange(int minTemp, int maxTemp){
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
    }

    public int getMinTemp() {
        return minTemp;
    }

    public int getMaxTemp() {
        return maxTemp;
    }

    public static TempRange fromTemperature(int temperature){
        for(TempRange range: values()){
            if(temperature >= range.getMinTemp() && temperature<= range.getMaxTemp()){
                return range;
            }
        }
        return temperature >28 ? VERY_HOT: VERY_COLD;
    }
}
