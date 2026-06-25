# 🗺️ Tripick — AI 여행 추천 서비스

> 축제도 여행지도, 당신 취향대로

사용자의 위치·관심사·이동수단을 입력받아 AI가 이번 달 딱 맞는 축제·여행지를 추천해주는 MSA 기반 웹 서비스입니다.

## 기술 스택

| 영역 | 기술 |
|---|---|
| Backend | Spring Boot 3.x, Java 17 |
| ORM | Spring Data JPA |
| DB | MySQL 8.x (Local: H2) |
| 캐싱 | Redis |
| 인증 | JWT (Access 1h / Refresh 7d) |
| Gateway | Spring Cloud Gateway |
| 외부 API | 공공데이터포털 전국문화축제표준데이터, 기상청 중기예보, Claude API, 카카오맵 |
| 문서화 | Swagger (springdoc) |
| 배포 | AWS EC2 + RDS |
| CI/CD | GitHub Actions |

## 프로젝트 구조 (Multi-module MSA)

```
tripick/
├── gateway-service/        # API Gateway, JWT 인증 필터        (8080)
├── auth-service/           # 회원가입·로그인·토큰 발급/재발급   (8081)
├── festival-service/       # 축제/여행지/북마크/리뷰           (8082)
├── recommendation-service/ # AI 추천 생성, festival-service 연동 (8083)
├── community-service/      # 여행 후기(UGC)·댓글·좋아요·신고   (8084)
├── notification-service/   # 알림 센터, 알림 수신 설정         (8085)
└── common/                 # 공통 응답/예외, JWT 유틸, 베이스 엔티티
```

각 서비스는 독립된 Spring Boot 애플리케이션이며, `common` 모듈을 공통 의존성으로 사용합니다.

## 브랜치 전략

| 브랜치 | 용도 |
|---|---|
| `main` | 최종 배포 브랜치 |
| `develop` | 개발 통합 브랜치 (서비스별 브랜치를 머지) |
| `auth-service`, `festival-service`, `gateway-service`, `recommendation-service`, `community-service`, `notification-service`, `common` | 서비스별 작업 브랜치 |

## 로컬 실행 방법

### 1. 환경변수 설정
```bash
# .env 파일 생성 (절대 커밋 금지!)
CULTURE_FESTIVAL_API_KEY=your-culture-festival-api-key
WEATHER_API_KEY=your-weather-api-key
CLAUDE_API_KEY=your-claude-api-key
KAKAO_APP_KEY=your-kakao-app-key
JWT_SECRET=your-jwt-secret-key-must-be-at-least-256-bits
```

### 2. 서비스별 실행 (local 프로파일, H2 인메모리 DB)
```bash
./gradlew :gateway-service:bootRun --args='--spring.profiles.active=local'
./gradlew :auth-service:bootRun --args='--spring.profiles.active=local'
./gradlew :festival-service:bootRun --args='--spring.profiles.active=local'
./gradlew :recommendation-service:bootRun --args='--spring.profiles.active=local'
./gradlew :community-service:bootRun --args='--spring.profiles.active=local'
./gradlew :notification-service:bootRun --args='--spring.profiles.active=local'
```

### 3. API 문서 확인
각 서비스의 `/swagger-ui.html` 에서 확인 (Gateway를 통해 통합 접근 예정)

## 개발 현황
- [x] 멀티모듈 Gradle 구조 세팅
- [x] common 모듈 (공통 응답/예외, JWT 유틸)
- [x] auth-service 회원가입/로그인/토큰 재발급/비밀번호 재설정 실구현
- [x] festival-service 축제/여행지/찜/후기 실구현
- [x] recommendation-service Claude API 연동 + festival-service Feign 연동으로 AI 추천 실구현
- [x] gateway-service JWT 인증 필터
- [x] community-service 후기 게시물/댓글/좋아요/신고 실구현
- [x] notification-service 알림센터/수신 설정 실구현
- [x] 전국문화축제표준데이터 + 기상청 API 연동
- [x] Claude API 연동
- [ ] 카카오맵 API 연동
- [ ] Redis 캐싱 적용
- [ ] AWS 배포
