
# 오늘의 옷장 (WishWardrobe)

> WebFlux 비동기 처리 기반 푸시 알림 시스템  
> 개인 프로젝트 | 2025.04 – 2026.02

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Backend | Java, Spring WebFlux |
| Database | Oracle, MongoDB |
| 외부 서비스 | Firebase Cloud Messaging, Web Push API |
| 인프라 | Docker, GitHub |
| Frontend | Vue.js |

---

## 핵심 문제 해결

### 1. FCM 스레드 풀 고갈 → 전체 서비스 장애 제거

**문제**  
부하 테스트 중 전체 API 응답 불가 현상 발생.  
Firebase SDK `.get()` 호출이 유효하지 않은 토큰에서 무한 대기 상태로 진입,  
스레드 풀을 점유하면서 정상 요청까지 처리 불가 상태로 전파됨.

**원인 분석**  
- Firebase Admin SDK의 `.get()`은 블로킹 호출
- 단일 토큰의 무한 대기가 전체 스레드 풀 고갈로 이어지는 구조적 결함
- 첫 수정 시도: `subscribeOn` + timeout 적용 → `InterruptedException`이 `onErrorDropped`로 무시되는 부작용 확인

**해결**  
`ApiFutures.addCallback` + `Mono.create`로 Future → Mono 타입 브릿징,  
블로킹 완전 제거 및 콜백 기반 비동기 처리로 전환.  
개별 FCM 응답 실측값 67–88ms 기준 5초 타임아웃 설정으로 단일 토큰 장애의 전체 서비스 전파 구조 차단.



---

### 2. WebPush 블로킹 I/O → 비동기 전환으로 처리 시간 94% 단축

**문제**  
WebPush 발송 건당 처리 시간 82초 확인 (JMeter 실측).

**원인 분석**  
`nl.martijndwars.webpush` 라이브러리가 내부적으로 블로킹 I/O 사용.  
구독자 수 증가 시 선형적 처리 시간 증가 구조.

**해결**  
`AsyncHttpClient` + `CompletableFuture` → `Mono.fromFuture()` 전환으로 비동기화.  
410 Gone 미처리 발견 → `HttpResponse.statusCode()` 직접 확인 후 만료 구독 즉시 제거.  
스케줄러 배치로 만료 구독 이중 정리, 불필요한 재전송 완전 차단.

| 지표 | 개선 전 | 개선 후 |
|------|---------|---------|
| 건당 처리 시간 | 82초 | 5초 |
| 개선율 | — | **94% 단축** |

---

### 3. Fan-out Concurrency 실측 최적화

**문제**  
다수 구독자 대상 브로드캐스트 시 concurrency 설정값 근거 없음.

**접근**  
concurrency 10 / 20 / 30 / 40 / 50 단계별 JMeter 부하 테스트 실측.

**결과**  
concurrency 30 채택: P95 응답시간 11.7%↓, 처리량 13.3%↑  
40 이상에서는 오히려 컨텍스트 스위칭 오버헤드로 성능 저하 확인.

---

## 프로젝트 구조

```
wishwardrobe-back/
├── notification-service/   # FCM, WebPush 비동기 처리
├── clothes-service/        # 옷장 관리
├── weather-service/        # 기상청 API 연동
└── docker-compose.yml
```

---

## 실행 방법

```bash
docker-compose up -d
```
