# Turing Arena (Backend)

> AI vs Human 판별 게임이 결합된 실시간 커뮤니티 플랫폼

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-6DB33F?style=flat&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Supported-2496ED?style=flat&logo=docker&logoColor=white)](https://www.docker.com/)

## 프로젝트 소개

**Turing Arena**는 익명 게시글을 읽고 AI가 작성했는지 인간이 작성했는지 추측하는 실시간 튜링 테스트 커뮤니티입니다. 사용자는 투표를 통해 점수를 얻고, 리더보드에서 순위를 확인할 수 있습니다.

### 핵심 컨셉트

- **AI vs Human 투표**: 각 게시글의 작성자가 AI인지 인간인지 투표
- **실시간 점수 시스템**: 정답 투표 시 즉각적인 점수 부여
- **익명 작성**: 관리자는 임의의 닉네임으로 게시글 작성 가능
- **정답 공개 시스템**: 일정 시간 후 정답 공개 및 투표 마감

### 프로젝트 목표

이 프로젝트는 다음을 목표로 합니다:

- RESTful API 설계 및 구현
- Spring Boot 기반 백엔드 아키텍처 구축
- JPA/Hibernate를 활용한 엔티티 관계 설계
- AWS S3 연동 파일 업로드 시스템
- JWT 기반 인증/인가 시스템
- Docker 기반 배포 환경 구성
- CI/CD 파이프라인 구축

---

## 주요 기능

### 1. AI vs Human 투표 시스템

| 기능 | 설명 |
|------|------|
| **투표** | 게시글마다 AI/인간 투표 가능 (1회 제한) |
| **점수 획득** | 정답 투표 시 즉시 점수 부여 |
| **투표 통계** | 실시간 투표 현황 (AI/인간 득표율) |
| **정답 공개** | 투표 마감 후 정답 및 결과 공개 |

### 2. 커뮤니티 기능

- **게시글 작성/수정/삭제**: 이미지 업로드 지원 (AWS S3)
- **댓글 및 대댓글**: 계층형 댓글 시스템
- **좋아요**: 게시글 좋아요 기능
- **조회수**: 게시글 조회수 추적
- **정렬 옵션**: 최신순, 조회수순 정렬

### 3. 사용자 관리

- **회원가입/로그인**: JWT 기반 인증
- **프로필 관리**: 닉네임, 프로필 이미지 변경
- **점수 시스템**: 투표 점수, 정답률 추적
- **랭킹 시스템**: 사용자별 순위 리더보드

### 4. 관리자 기능

- **익명 게시글 작성**: 임의의 닉네임으로 AI/인간 게시글 작성
- **정답 설정**: AI 또는 인간 작성자 유형 설정
- **투표 마감 시간 설정**: 게시글별 투표 기한 설정
- **수동 정답 공개**: 관리자가 직접 정답 공개 가능

---

## 기술 스택

### Backend Core
- **Java 21**: 최신 LTS 버전
- **Spring Boot 3.5.6**: 프레임워크
- **Spring Data JPA**: ORM 및 데이터 액세스
- **QueryDSL**: 타입 안전한 동적 쿼리
- **Spring Security**: 인증/인가
- **MySQL 8.0**: 관계형 데이터베이스

### Infrastructure
- **AWS S3**: 이미지 파일 저장
- **Docker**: 컨테이너 기반 배포
- **GitHub Actions**: CI/CD 파이프라인
- **Spring Boot Actuator**: 헬스 체크 및 모니터링

### Documentation & Tools
- **Swagger (SpringDoc)**: API 문서 자동화
- **Lombok**: 보일러플레이트 코드 감소
- **Gradle**: 빌드 도구

---

## 프로젝트 구조

```
src/main/java/com/ktb/community/
├── controller/          # REST API 컨트롤러
│   ├── PostController.java
│   ├── VoteController.java
│   ├── UserController.java
│   ├── CommentController.java
│   └── AuthController.java
├── service/             # 비즈니스 로직
│   ├── PostService.java
│   ├── PostVoteService.java
│   ├── UserScoreService.java
│   ├── CommentService.java
│   └── UserService.java
├── repository/          # 데이터 액세스 레이어
│   ├── PostRepository.java
│   ├── PostVoteRepository.java
│   ├── UserScoreRepository.java
│   └── CommentRepository.java
├── entity/              # JPA 엔티티
│   ├── Post.java
│   ├── PostVote.java
│   ├── UserScore.java
│   ├── Comment.java
│   ├── User.java
│   ├── AuthorType.java   # AI/HUMAN enum
│   └── VoteType.java     # AI/HUMAN enum
├── dto/                 # 데이터 전송 객체
│   ├── post/
│   ├── vote/
│   ├── comment/
│   └── user/
├── config/              # 설정 클래스
│   ├── SecurityConfig.java
│   ├── SwaggerConfig.java
│   └── S3Config.java
└── exception/           # 예외 처리
    └── GlobalExceptionHandler.java
```

---

## 데이터베이스 설계

### ERD 다이어그램

```
┌─────────────────────┐
│       User          │
│─────────────────────│
│ id (PK)             │
│ email               │
│ nickname            │
│ is_admin            │
└─────────────────────┘
         │
         │ 1:1
         ▼
┌─────────────────────────────┐
│   UserScore                 │
│─────────────────────────────│
│ user_id (PK, FK)            │
│ vote_score                  │  ← 투표 점수
│ total_votes                 │  ← 총 투표 수
│ correct_votes               │  ← 정답 투표 수
└─────────────────────────────┘
         ▲
         │ N:1
         │
┌─────────────────────────────┐
│       PostVote              │
│─────────────────────────────│
│ id (PK)                     │
│ user_id (FK)                │
│ post_id (FK)                │
│ vote_type (AI/HUMAN)        │  ← 사용자가 투표한 타입
│ is_correct                  │  ← 정답 여부
│ UNIQUE(user_id, post_id)    │  ← 중복 투표 방지
└─────────────────────────────┘
         │
         │ N:1
         ▼
┌─────────────────────────────────┐
│       Post                      │
│─────────────────────────────────│
│ id (PK)                         │
│ user_id (FK)                    │
│ author_type                     │  ← AI 또는 HUMAN (정답)
│ custom_author_name              │  ← 익명 닉네임
│ vote_deadline_at                │  ← 투표 마감 시각
│ answer_revealed_at              │  ← 정답 공개 시각
└─────────────────────────────────┘
         │
         │ 1:1
         ▼
┌──────────────────────────────────────┐
│       PostStats                      │
│──────────────────────────────────────│
│ post_id (PK, FK)                     │
│ ai_vote_count                        │  ← AI 투표 수
│ human_vote_count                     │  ← 인간 투표 수
│ like_count                           │
│ view_count                           │
└──────────────────────────────────────┘
```

### 주요 테이블

#### Post (게시글)
- `author_type`: AI 또는 HUMAN (정답)
- `custom_author_name`: 관리자가 설정한 익명 닉네임
- `vote_deadline_at`: 투표 마감 시각
- `answer_revealed_at`: 정답 공개 시각

#### PostVote (투표 기록)
- 사용자별 게시글당 1회 투표 제한 (`UNIQUE(user_id, post_id)`)
- `is_correct`: 투표 시점에 자동 계산되어 저장
- 투표 취소 불가 (불변성 보장)

#### UserScore (사용자 점수)
- `vote_score`: 정답 투표로 획득한 점수
- `total_votes`: 전체 투표 수
- `correct_votes`: 정답 투표 수
- 정확도 계산: `(correct_votes / total_votes) * 100`

---

## API 명세

### 투표 관련 API

#### POST /api/posts/{postId}/vote
게시글에 AI/인간 투표

**Request:**
```json
{
  "voteType": "AI"  // "AI" or "HUMAN"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "voteId": 123,
    "voteType": "AI",
    "isCorrect": true,
    "userTotalScore": 42,
    "postVoteStats": {
      "aiVoteCount": 15,
      "humanVoteCount": 8,
      "totalVoteCount": 23
    },
    "createdAt": "2025-12-06T10:30:00Z"
  }
}
```

#### GET /api/users/me/vote-score
현재 사용자의 투표 점수 조회

**Response:**
```json
{
  "success": true,
  "data": {
    "userId": 123,
    "nickname": "user123",
    "voteScore": 42,
    "totalVotes": 50,
    "correctVotes": 42,
    "accuracy": 84.0
  }
}
```

#### GET /api/rankings
사용자 랭킹 조회

**Response:**
```json
{
  "success": true,
  "data": {
    "rankings": [
      {
        "rank": 1,
        "userId": 123,
        "nickname": "user123",
        "voteScore": 100,
        "accuracy": 90.5
      }
    ],
    "currentUserRank": {
      "rank": 15,
      "voteScore": 42
    }
  }
}
```

### 게시글 관련 API

#### POST /api/posts
게시글 작성 (관리자는 익명 작성 가능)

**Request:**
```json
{
  "title": "제목",
  "content": "내용",
  "fileIds": [1, 2],
  // Admin 전용 필드
  "authorType": "AI",          // "AI" or "HUMAN"
  "customAuthorName": "익명123",
  "voteDeadlineHours": 24      // 24시간 후 투표 마감
}
```

#### GET /api/posts/{postId}
게시글 상세 조회

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "제목",
    "authorName": "익명123",
    "isCustomAuthor": true,
    "authorType": "HUMAN",        // 정답 공개 후에만 노출
    "answerRevealed": true,
    "voteDeadlineAt": "2025-12-07T10:00:00Z",
    "currentUserVote": {
      "voteType": "AI",
      "isCorrect": false
    },
    "voteStats": {
      "aiVoteCount": 20,
      "humanVoteCount": 30,
      "totalVoteCount": 50
    }
  }
}
```

### 관리자 API

#### POST /api/admin/posts/{postId}/reveal-answer
정답 수동 공개 (Admin 전용)

**Response:**
```json
{
  "success": true,
  "data": {
    "postId": 1,
    "authorType": "HUMAN",
    "answerRevealedAt": "2025-12-06T11:00:00Z",
    "correctVoters": 30,
    "incorrectVoters": 20
  }
}
```

전체 API 문서는 Swagger UI에서 확인 가능: `http://localhost:8080/swagger-ui.html`

---

## 시작하기

### 사전 요구사항

- Java 21 이상
- MySQL 8.0 이상
- Docker (선택사항)

### 환경 변수 설정

`src/main/resources/application-key.yml` 파일 생성:

```yaml
spring:
  datasource:
    password: your_db_password

jwt:
  secret: your_jwt_secret_key

aws:
  s3:
    access-key: your_aws_access_key
    secret-key: your_aws_secret_key
```

### 로컬 실행

```bash
# 1. 저장소 클론
git clone https://github.com/100-hours-a-week/3-jun-bae-community.git
cd 3-jun-bae-community

# 2. MySQL 데이터베이스 생성
mysql -u root -p
CREATE DATABASE community;

# 3. 애플리케이션 실행
./gradlew bootRun

# 4. API 접속
# http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

### Docker 실행

```bash
# 1. Docker 이미지 빌드
docker build -t turing-arena-backend .

# 2. Docker Compose로 실행
docker-compose up -d

# MySQL과 Spring Boot 애플리케이션이 함께 실행됩니다
```

---

## 주요 비즈니스 로직

### 투표 프로세스

```
1. 사용자 투표 요청
   ↓
2. 검증 (게시글 존재, 투표 마감 여부, 중복 투표)
   ↓
3. 정답 계산 (isCorrect = voteType == post.authorType)
   ↓
4. PostVote 엔티티 저장
   ↓
5. PostStats 투표 수 증가
   ↓
6. UserScore 점수 부여
   - 정답: vote_score+1, correct_votes+1, total_votes+1
   - 오답: total_votes+1만 증가
   ↓
7. 투표 결과 응답
```

### 정답 공개 프로세스

- **자동 공개**: 스케줄러가 `vote_deadline_at` 도달 시 자동 공개 (선택사항)
- **수동 공개**: 관리자가 `/api/admin/posts/{postId}/reveal-answer` API 호출
- 정답 공개 시 `answer_revealed_at` 타임스탬프 기록

### 동시성 제어

- **중복 투표 방지**: `UNIQUE(user_id, post_id)` 제약조건
- **통계 업데이트**: PostStats 업데이트 시 낙관적 락 사용
- **트랜잭션 관리**: `@Transactional`로 데이터 일관성 보장

---

## 프론트엔드 연동

이 백엔드는 다음 프론트엔드 프로젝트와 함께 동작합니다:

**Frontend Repository**: [3-jun-bae-community-fe](https://github.com/100-hours-a-week/3-jun-bae-community-fe)

### 기술 스택
- Vanilla JavaScript (프레임워크 없이 순수 JS)
- Pico.css (미니멀 CSS 프레임워크)
- Express.js (프록시 서버)

### CORS 설정
프론트엔드와 연동을 위해 `SecurityConfig.java`에 CORS 설정이 포함되어 있습니다:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    // ...
}
```

---

## CI/CD 파이프라인

GitHub Actions를 사용한 자동 배포 파이프라인:

```yaml
# .github/workflows/deploy.yml
- Build: Gradle로 JAR 빌드
- Docker: 이미지 빌드 및 푸시
- Deploy: 서버에 배포
- Health Check: Actuator를 통한 헬스 체크
```

**배포 과정**:
1. `main` 브랜치에 Push
2. GitHub Actions 워크플로우 실행
3. Docker 이미지 빌드
4. 서버에 컨테이너 배포
5. 자동 헬스 체크

---

## 테스트

### 단위 테스트
```bash
./gradlew test
```

### API 테스트
Swagger UI 또는 Postman을 사용하여 API 테스트 가능

---

## 향후 개선 사항

### 기능 확장
- [ ] 주간/월간 랭킹 시스템
- [ ] 난이도별 차등 점수 시스템
- [ ] 빠른 투표 보너스 점수
- [ ] 게시글별 정답률 분석

### 성능 최적화
- [ ] Redis 캐싱 (랭킹, 통계)
- [ ] 데이터베이스 인덱스 최적화
- [ ] N+1 쿼리 최적화

### 인프라
- [ ] Kubernetes 배포
- [ ] AWS RDS 마이그레이션
- [ ] CloudFront CDN 적용
- [ ] 모니터링 (Prometheus + Grafana)

---

## 프로젝트 하이라이트

### 설계 역량
- JPA를 활용한 엔티티 관계 설계 (1:1, 1:N, N:1)
- 투표 불변성을 보장하는 데이터 무결성 설계
- 동시성 제어를 고려한 통계 업데이트 로직

### 백엔드 개발
- RESTful API 설계 원칙 준수
- Spring Security + JWT 기반 인증/인가
- QueryDSL을 활용한 동적 쿼리 작성
- AWS S3 연동 파일 업로드 시스템

### DevOps
- Docker 기반 컨테이너화
- GitHub Actions CI/CD 파이프라인
- 헬스 체크 및 모니터링 엔드포인트

---

## 라이선스

이 프로젝트는 포트폴리오 목적으로 제작되었습니다.

---

## 작성자

**Jun Bae** | Backend Developer

- GitHub: [@baejun10](https://github.com/baejun10)
- Portfolio: [Turing Arena - AI vs Human Community](https://github.com/100-hours-a-week/3-jun-bae-community)

---

## 참고 문서

- [API 설계 문서](docs/AI_VOTING_SYSTEM_DESIGN.md)
- [통계 최적화 전략](docs/STATS_OPTIMIZATION_STRATEGIES.md)
- [Redis 랭킹 전략](docs/REDIS_RANKING_STRATEGIES.md)
- [Frontend Repository](https://github.com/100-hours-a-week/3-jun-bae-community-fe)
