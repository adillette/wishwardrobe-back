# WishWardrobe MSA 프로젝트 설정 가이드

## 🏗️ 아키텍처

이 프로젝트는 MSA(Microservices Architecture) 방식으로 구성되어 있습니다:

- **Frontend**: Vue.js (Nginx로 서빙, 개발 서버는 8080 포트)
- **Clothes Service**: Spring WebMVC (8081 포트)
- **Weather Service**: Spring WebFlux (8082 포트)
- **Notification Service**: Spring WebFlux (8083 포트)
- **Database**: MongoDB (날씨, 알림 데이터)
- **Cache**: Redis (캐싱 및 세션 관리)

## 🚀 시작하기

### 1. 프론트엔드 환경 설정

```bash
cd wishwardrobe-front
cp .env.example .env
```

`.env` 파일을 열어 API 키를 설정하세요:
```env
VUE_APP_WEATHER_API_KEY=공공데이터포털_API_키
VUE_APP_KAKAO_MAPS_API_KEY=카카오맵_API_키
```

### 2. Docker로 전체 서비스 실행

```bash
cd MSA
docker-compose up -d
```

### 3. 개발 환경에서 프론트엔드만 실행

백엔드 MSA 서비스들은 Docker로 실행하고, 프론트엔드만 개발 서버로 실행:

```bash
# 백엔드 서비스들 시작
cd MSA
docker-compose up -d clothes-service weather-service notification-service mongodb redis

# 프론트엔드 개발 서버 시작
cd ../wishwardrobe-front
npm install
npm run serve
```

프론트엔드는 자동으로 localhost:8081, 8082, 8083 포트로 백엔드 서비스에 연결됩니다.

## 📡 API 엔드포인트

### Weather Service (WebFlux)
- **GET** `/weather?location={동이름}` - 날씨 정보 조회
  - 예: `/weather?location=역삼동`
  - 응답: WeatherForecastDTO (날짜, 온도, 습도, 강수확률 등)

### Clothes Service (WebMVC)
- **GET** `/clothes` - 의류 목록 조회
- **POST** `/clothes` - 의류 추가
- **DELETE** `/clothes/{id}` - 의류 삭제

### Notification Service (WebFlux)
- **GET** `/notifications` - 알림 목록 조회
- **POST** `/notifications` - 알림 추가

## 🌐 접근 URL

- **프론트엔드**: http://localhost:8080
- **Clothes Service**: http://localhost:8081
- **Weather Service**: http://localhost:8082
- **Notification Service**: http://localhost:8083
- **MongoDB**: localhost:27017
- **Redis**: localhost:6379

## 🔧 프론트엔드-백엔드 연결

### 개발 환경
프론트엔드의 `axiosConfig.js`가 자동으로 localhost의 각 포트로 직접 연결합니다.

### Docker 환경
Nginx가 프록시 역할을 하여 `/api/weather`, `/api/clothes`, `/api/notification` 경로로 요청을 각 서비스로 라우팅합니다.

## 📅 날짜 표시 기능

Weather.vue 컴포넌트에서 백엔드로부터 받은 날씨 데이터의 날짜 정보를 화면에 표시합니다:

- **예보 날짜**: `forecastDate` (LocalDate)
- **예보 시간**: `forecastTime` (LocalTime)
- **기준 날짜**: `baseDate` (LocalDate)
- **기준 시간**: `baseTime` (LocalTime)

날짜는 "12월 27일 (금)" 형식으로, 시간은 "오후 3:00" 형식으로 표시됩니다.

## 🛠️ 트러블슈팅

### CORS 오류 발생 시
각 백엔드 서비스의 `CorsConfig.java`에서 허용된 오리진을 확인하세요.

### Docker 네트워크 오류 시
```bash
docker-compose down
docker network prune
docker-compose up -d
```

### 프론트엔드에서 백엔드 연결 안 될 때
1. 백엔드 서비스들이 정상 실행 중인지 확인
2. 브라우저 콘솔에서 네트워크 탭 확인
3. API URL이 올바르게 설정되었는지 확인

## 📝 참고사항

- Weather Service와 Notification Service는 반응형 프로그래밍을 위해 WebFlux를 사용합니다.
- Clothes Service는 전통적인 WebMVC 패턴을 사용합니다.
- 모든 서비스는 `wishwordrobe-network`라는 Docker 네트워크로 연결됩니다.
