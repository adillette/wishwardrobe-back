# 🌤️ 기상청 API 테스트 가이드

## 📋 테스트 목적
1. ✅ 기상청 API 연결 확인
2. ✅ 위경도 → 격자좌표 변환 확인
3. ✅ Weather Service 엔드포인트 동작 확인

---

## 🚀 테스트 순서

### 1단계: 기상청 API 직접 테스트
먼저 기상청 API가 정상적으로 응답하는지 확인합니다.

#### 테스트 1-1: 초단기실황 조회
```
GET http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst
```
- **목적**: 기상청 API 연결 확인
- **파라미터**:
  - nx=60, ny=127 (서울 시청)
  - base_date: 오늘 날짜 (YYYYMMDD)
  - base_time: 현재 시각 (HHMM)

**✅ 성공 응답 예시:**
```json
{
  "response": {
    "header": {
      "resultCode": "00",
      "resultMsg": "NORMAL_SERVICE"
    },
    "body": {
      "items": {
        "item": [
          {
            "baseDate": "20241230",
            "baseTime": "1400",
            "category": "T1H",
            "nx": 60,
            "ny": 127,
            "obsrValue": "5.0"
          }
        ]
      }
    }
  }
}
```

**❌ 오류 코드:**
- `01`: 서비스 KEY가 유효하지 않음
- `03`: 등록되지 않은 서비스 KEY
- `04`: 활용기간 만료
- `05`: IP 제한

---

### 2단계: Weather Service 테스트

#### 테스트 2-2: 위경도로 날씨 조회 (서울시청)
```
GET http://localhost:8082/api/v1/weather/coordinates?lat=37.5665&lon=126.9780
```

**내부 처리 흐름:**
1. 위경도 입력: (126.9780, 37.5665)
2. 격자 변환: nx=60, ny=127
3. 기상청 API 호출
4. 날씨 데이터 응답

**✅ 로그 확인:**
```
위경도로 날씨 조회 요청: lat=37.5665, lon=126.9780
위경도 (126.978, 37.5665) -> 격자 (60, 127)
날씨 정보 조회 성공
```

---

## 📍 주요 테스트 좌표

| 지역 | 위도 | 경도 | 예상 격자(nx, ny) |
|------|------|------|-------------------|
| 서울 시청 | 37.5665 | 126.9780 | (60, 127) |
| 강남역 | 37.4979 | 127.0276 | (61, 126) |
| 부산 시청 | 35.1796 | 129.0756 | (98, 76) |
| 제주 시청 | 33.4996 | 126.5312 | (52, 38) |
| 인천공항 | 37.4691 | 126.4505 | (55, 124) |

---

## 🔍 테스트 체크리스트

### ✅ 기상청 API 연결 확인
- [ ] API Key가 유효한가?
- [ ] resultCode가 "00"인가?
- [ ] 응답 시간이 5초 이내인가?

### ✅ 좌표 변환 확인
- [ ] 위경도가 올바른 격자좌표로 변환되는가?
- [ ] 로그에 변환 정보가 출력되는가?

### ✅ Weather Service 응답 확인
- [ ] HTTP 200 응답을 받는가?
- [ ] 날씨 데이터가 포함되어 있는가?
- [ ] 온도, 습도, 강수 정보가 있는가?

---

## 🛠️ Postman 사용법

### 1. 컬렉션 임포트
1. Postman 실행
2. Import 버튼 클릭
3. `weather-api-test.postman_collection.json` 파일 선택
4. Import 완료

### 2. 자동 변수 설정
컬렉션에는 Pre-request Script가 포함되어 있어 자동으로 설정됩니다:
- `{{currentDate}}`: 오늘 날짜 (YYYYMMDD)
- `{{currentTime}}`: 현재 시각 (HHMM)

### 3. 테스트 실행
1. "1. 기상청 API 직접 테스트" → "1-1. 초단기실황 조회" 실행
2. 응답 확인 (resultCode: "00")
3. "2. Weather Service 테스트" → "2-2. 위경도로 날씨 조회" 실행
4. 로그에서 좌표 변환 확인

### 4. 자동 테스트 검증
각 요청 실행 시 자동으로 검증됩니다:
- ✅ 응답 시간 < 5초
- ✅ 상태 코드 = 200
- ✅ JSON 파싱 성공

---

## 🎯 프론트엔드 연동 시나리오

### 시나리오: 사용자 현재 위치 기반 날씨 조회

```javascript
// 1. 프론트엔드에서 위치 정보 가져오기
navigator.geolocation.getCurrentPosition(async (position) => {
  const lat = position.coords.latitude;   // 예: 37.5665
  const lon = position.coords.longitude;  // 예: 126.9780
  
  // 2. Weather Service API 호출
  const response = await fetch(
    `http://localhost:8082/api/v1/weather/coordinates?lat=${lat}&lon=${lon}`
  );
  
  // 3. 날씨 데이터 받기
  const weatherData = await response.json();
  console.log(weatherData);
});
```

**백엔드 처리:**
1. ✅ 위경도 수신: (126.9780, 37.5665)
2. ✅ 격자 변환: WeatherGridConverter → (60, 127)
3. ✅ 기상청 API 호출: nx=60, ny=127
4. ✅ 날씨 데이터 반환

---

## 📊 기상청 API 정보

### 제공 카테고리
- `T1H`: 기온 (℃)
- `RN1`: 1시간 강수량 (mm)
- `SKY`: 하늘상태 (1:맑음, 3:구름많음, 4:흐림)
- `REH`: 습도 (%)
- `PTY`: 강수형태 (0:없음, 1:비, 2:비/눈, 3:눈)
- `WSD`: 풍속 (m/s)

### 발표 시각
- **초단기실황**: 매시간 40분
- **초단기예보**: 매시간 30분
- **단기예보**: 02, 05, 08, 11, 14, 17, 20, 23시

---

## ❓ 트러블슈팅

### 문제: API Key 오류
```json
{
  "response": {
    "header": {
      "resultCode": "03",
      "resultMsg": "NODATA_ERROR"
    }
  }
}
```
**해결**: application.yml의 API Key 확인

### 문제: 좌표 범위 오류
```
IllegalArgumentException: 격자 범위 초과
```
**해결**: 대한민국 영역 내 좌표인지 확인
- 위도: 33° ~ 43°
- 경도: 124° ~ 132°

### 문제: 시간 오류
```json
{
  "resultCode": "05",
  "resultMsg": "SERVICETIMEOUT_ERROR"
}
```
**해결**: 발표 시각 이후에 요청 (초단기실황: 매시 40분 이후)

---

## 🎉 테스트 성공 기준

✅ **모든 조건을 만족하면 성공:**
1. 기상청 API 직접 호출 성공 (resultCode: "00")
2. 위경도 → 격자좌표 변환 성공 (로그 확인)
3. Weather Service 응답 성공 (HTTP 200)
4. 날씨 데이터 포함 확인

---

## 📞 지원

문제가 발생하면 다음을 확인하세요:
1. weather-service가 실행 중인가? (포트 8082)
2. MongoDB가 실행 중인가?
3. Redis가 실행 중인가?
4. 기상청 API Key가 유효한가?

---

**작성일**: 2024-12-30
**버전**: 1.0
