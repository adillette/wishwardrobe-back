# WishWardrobe

## Webflux , Push api , MSA 프로젝트

#요구사항
- 현재 내 위치에 맞는 오전 7:00 푸시 메시지 발송(push api 적용)
- 공공 api(기상청 api) 적용하여 날씨 정보 끌어오기
- 현재 내 위치 적용하기 위해 카카오맵 api 사용하여 내위치 찾기
- push 메시지 내용: 오늘 아침 기온/ 오늘의 최고 기온과 추천 옷차림
- 옷차림 based by  내 옷장에 들어있는 옷 알려준다
- 추천 옷차림 ai 있는지 확인

# 프로젝트 개요
- 1인 프로젝트
- MSA를 활용해서 개별 프로그램이 유기적으로 움직이는 프로그램 지향,

- 개별 사용자별 옷장 서비스 제공, 날씨 정보를 출근 시간에

  사용자 위치 적용하여 현재 기온~ 오늘의 최고 기온, 오늘 출근에 입을 내 옷장 안의 옷을 추천받도록 한다.

# 기술 스택
![java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![spring](https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![oracle](https://img.shields.io/badge/Oracle-F80000?style=for-the-badge&logo=oracle&logoColor=black)
![mongodb](https://img.shields.io/badge/MongoDB-4EA94B?style=for-the-badge&logo=mongodb&logoColor=white)
![redis](https://img.shields.io/badge/redis-%23DD0031.svg?&style=for-the-badge&logo=redis&logoColor=white)
![docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
![react](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)

# 주요 기능
- 알림서비스
- 옷장서비스
- 로그인 서비스
- 날씨 서비스

# 결과 및 성과

-실시간 위치 기반 정보 제공을 위한 카카오맵 API와 기상청 공공 API 연동

- FCM을 통한 푸시 알림 서비스 설계
  
		- 브라우저 환경 푸시 알림 처리, Service Worker 활용
  
		- Redis 기반 디바이스 토큰 관리 및 알림 발송 이력 관리
  
- 서비스 특성에 맞는 Redis 캐싱 패턴 설계 및 적용
  
		- 조회 빈도가 높은 데이터 Look-aside패턴 조회 성능 최적화
  
		- 업데이트 많은 데이터 Write-back패턴 쓰기 성능 최적화
  
		- 각 서비스별 독립적 캐시 관리에 Redis 활용
  
- 동기식/비동기식 DB의 Configuration 분리, 멀티 데이터베이스 운영
  
- WebFlux 사용하여 시스템 확장성 구현

- 단위테스트로 안정적인 코드베이스 유지
  
- 비동기 메시징 아키텍처 구축, RabbitMQ Exchange 활용
