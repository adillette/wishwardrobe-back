# FCM 푸시 알림 테스트 가이드

## 📱 방법 1: 모바일 앱에서 테스트 (Android)

### 1. Firebase 프로젝트 설정
1. Firebase Console (https://console.firebase.google.com) 접속
2. 프로젝트 설정 → Cloud Messaging → API 활성화
3. Android 앱 추가:
   - 패키지 이름: `com.wishwordrobe.test`
   - `google-services.json` 다운로드

### 2. 간단한 Android 앱 만들기

**build.gradle (프로젝트 레벨)**
```gradle
plugins {
    id 'com.google.gms.google-services' version '4.4.0'
}

dependencies {
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-messaging'
}
```

**MainActivity.kt**
```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // FCM 토큰 가져오기
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM Token", token)
                Toast.makeText(this, "토큰: $token", Toast.LENGTH_LONG).show()
                // 이 토큰을 복사해서 Postman에서 사용
            }
        }
    }
}
```

---

## 🌐 방법 2: 웹 브라우저에서 테스트 (가장 간단!)

### 1. Firebase 웹 앱 설정
1. Firebase Console → 프로젝트 설정
2. 웹 앱 추가 (</>)
3. 앱 등록 후 Firebase 구성 정보 복사
4. Cloud Messaging → 웹 푸시 인증서 → 키 쌍 생성 (VAPID key)

### 2. 테스트 파일 수정
이 폴더에 있는 `fcm-test.html`과 `firebase-messaging-sw.js`를 수정:

1. **fcm-test.html 수정**
   - `YOUR_API_KEY`, `YOUR_PROJECT_ID` 등을 본인의 Firebase 정보로 변경
   - `YOUR_VAPID_KEY`를 웹 푸시 인증서 키로 변경

2. **firebase-messaging-sw.js 수정**
   - Firebase 구성 정보 입력

### 3. 로컬 서버 실행
```bash
# Python이 설치되어 있다면:
cd notification-service
python -m http.server 8000

# 또는 Node.js의 http-server:
npx http-server -p 8000
```

### 4. 브라우저에서 테스트
1. http://localhost:8000/fcm-test.html 접속
2. "알림 권한 요청" 클릭 → 허용
3. "FCM 토큰 가져오기" 클릭
4. 토큰 복사

---

## 📮 방법 3: Postman으로 푸시 전송

### 1. FCM 토큰으로 푸시 보내기

**엔드포인트**: `POST http://localhost:8083/api/notification/send-token`

**Headers**:
```
Content-Type: application/json
```

**Body (JSON)**:
```json
{
  "title": "날씨 알림",
  "message": "오늘은 맑은 날씨입니다. 가벼운 옷차림을 추천합니다!",
  "icon": "https://example.com/weather-icon.png",
  "clickAction": "https://wishwordrobe.today",
  "token": "여기에_FCM_토큰_붙여넣기",
  "data": {
    "temperature": "15",
    "location": "서울"
  }
}
```

### 2. 토픽 구독자에게 푸시 보내기

먼저 앱에서 토픽 구독:
```kotlin
// Android
FirebaseMessaging.getInstance().subscribeToTopic("weather-updates")
```

**엔드포인트**: `POST http://localhost:8083/api/notification/send-topic`

**Body (JSON)**:
```json
{
  "title": "날씨 업데이트",
  "message": "내일은 비가 예상됩니다. 우산을 챙기세요!",
  "topic": "weather-updates",
  "data": {
    "type": "weather",
    "severity": "normal"
  }
}
```

---

## 🧪 테스트 순서 (추천)

### 단계별 테스트:

1. **서비스 실행 확인**
   ```bash
   cd notification-service
   ./gradlew bootRun
   ```

2. **헬스체크**
   ```
   GET http://localhost:8083/api/test/health
   ```

3. **웹 브라우저로 FCM 토큰 받기**
   - 위의 웹 방법 따라하기 (가장 빠름!)
   - 또는 Firebase Console에서 직접 테스트 메시지 보내기

4. **Postman으로 실제 푸시 전송**
   - 받은 토큰으로 `/send-token` API 호출

5. **결과 확인**
   - 웹: 브라우저 알림 표시됨
   - 모바일: 푸시 알림 표시됨

---

## 🔧 문제 해결

### "service-account-key.json not found"
```bash
# Firebase Console → 프로젝트 설정 → 서비스 계정
# → 새 비공개 키 생성 → JSON 다운로드
# → notification-service/src/main/resources/에 저장
```

### 알림이 안 옴
1. FCM 토큰이 유효한지 확인
2. Firebase Console에서 직접 테스트 메시지 전송해보기
3. 앱이 백그라운드/포그라운드 상태 확인
4. 알림 권한 허용 여부 확인

### CORS 에러 (웹 테스트 시)
```bash
# 로컬 서버로 실행해야 함 (file:// 프로토콜 사용 불가)
python -m http.server 8000
```

---

## 📞 빠른 테스트 방법 (5분 완성)

1. Firebase Console → Cloud Messaging
2. "Send your first message" 클릭
3. 알림 제목/내용 입력
4. "Send test message" 클릭
5. FCM 토큰 입력 (앱/웹에서 받은 토큰)
6. "Test" 클릭

이 방법으로 빠르게 FCM이 정상 작동하는지 확인 가능!
