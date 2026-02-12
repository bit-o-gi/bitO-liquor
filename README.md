# Liquor Backend

한국 이커머스 사이트(롯데온, 이마트)에서 주류 가격 정보를 크롤링하여 가격 비교 및 추적을 제공하는 백엔드 서비스입니다.

## 주요 기능

- 롯데온/이마트 주류 실시간 크롤링
- 가격 비교 및 검색
- 가격 변동 이력 추적
- 브랜드/카테고리별 필터링
- 최저가 위스키 조회
- REST API 제공

## 기술 스택

- Java 21
- Spring Boot 3.5.6
- Selenium WebDriver (웹 크롤링)
- Jsoup (HTML 파싱)
- PostgreSQL (Supabase)
- Gradle (멀티모듈)

## 멀티모듈 구조

```
liquor-backend/
├── common/    — 공통 라이브러리 (엔티티, 리포지토리, DTO)
├── api/       — 조회/검색 API 애플리케이션 (포트 8080)
└── crawler/   — 크롤링 애플리케이션 (포트 8081)
```

## 사전 요구사항

### Chrome 브라우저 설치
Selenium을 사용하기 위해 Chrome 브라우저가 설치되어 있어야 합니다.
Selenium 4.x 버전은 WebDriver Manager를 내장하고 있어 ChromeDriver를 자동으로 다운로드합니다.

### 데이터베이스 설정
`.env.example`을 참고하여 환경변수를 설정하세요:
```
DB_URL=jdbc:postgresql://your-project.supabase.co:6543/postgres
DB_USERNAME=postgres.your-project-ref
DB_PASSWORD=your-password
```

## 실행 방법

### 1. 빌드
```bash
./gradlew build
```

### 2. API 서버 실행 (조회/검색)
```bash
./gradlew :api:bootRun
```
서버는 `http://localhost:8080`에서 실행됩니다.

### 3. 크롤러 서버 실행
```bash
./gradlew :crawler:bootRun
```
서버는 `http://localhost:8081`에서 실행됩니다.

## API 사용법

### 조회 API — api 모듈 (`:8080`)

#### 주류 (`/api/liquors`)

```bash
# 전체 주류 조회
curl http://localhost:8080/api/liquors

# 주류 검색
curl "http://localhost:8080/api/liquors/search?q=조니워커"

# 브랜드별 조회
curl http://localhost:8080/api/liquors/brand/조니워커

# 카테고리별 조회
curl http://localhost:8080/api/liquors/category/Whisky

# 저렴한 주류 조회
curl "http://localhost:8080/api/liquors/cheap?max=50000"

# 가격 이력 조회
curl http://localhost:8080/api/liquors/1/history
```

#### 위스키 (`/api/whisky`)

```bash
# 최저가 위스키 조회
curl http://localhost:8080/api/whisky/lowest
```

### 크롤링 API — crawler 모듈 (`:8081`)

```bash
# 롯데온 크롤링 실행
curl -X POST http://localhost:8081/api/crawl/lotteon

# 이마트 크롤링 실행
curl -X POST http://localhost:8081/api/crawl/emart
```

## 프로젝트 구조

```
liquor-backend/
├── common/src/main/java/org/bito/liquor/common/
│   ├── dto/
│   │   └── LiquorDto.java              # 응답 DTO
│   ├── model/
│   │   ├── Liquor.java                 # 주류 엔티티
│   │   └── PriceHistory.java           # 가격 이력 엔티티
│   └── repository/
│       ├── LiquorRepository.java
│       └── PriceHistoryRepository.java
├── api/src/main/java/org/bito/liquor/
│   ├── ApiApplication.java             # API 메인 애플리케이션
│   ├── config/
│   │   └── WebConfig.java              # CORS 설정
│   ├── controller/
│   │   ├── LiquorController.java       # 주류 조회 API
│   │   └── WhiskyController.java       # 위스키 조회 API
│   └── service/
│       └── LiquorQueryService.java     # 주류 조회 서비스
└── crawler/src/main/java/org/bito/liquor/
    ├── CrawlerApplication.java         # 크롤러 메인 애플리케이션
    ├── controller/
    │   ├── LotteonCrawlController.java # 롯데온 크롤링 API
    │   └── EmartCrawlController.java   # 이마트 크롤링 API
    ├── scraper/
    │   ├── LotteonScraper.java         # 롯데온 크롤러
    │   └── EmartScraper.java           # 이마트 크롤러
    └── service/
        ├── LotteonCrawlService.java    # 롯데온 크롤링 서비스
        └── EmartCrawlService.java      # 이마트 크롤링 서비스
```

## 주의사항

### 웹 크롤링 관련
- 각 사이트의 이용약관을 준수하세요
- 과도한 요청은 서버에 부담을 줄 수 있습니다
- 개인 학습 목적으로만 사용하세요

### 기술적 제한사항
- 사이트의 HTML 구조가 변경되면 크롤러 수정이 필요합니다
- 헤드리스 Chrome을 사용하므로 시스템 리소스를 사용합니다

## 문제 해결

### ChromeDriver 에러
```
Could not start a new session. Selenium Manager failed
```

**해결 방법:**
1. Chrome 브라우저를 최신 버전으로 업데이트
2. 수동으로 ChromeDriver 다운로드: https://chromedriver.chromium.org/
3. PATH에 ChromeDriver 경로 추가

## 테스트

```bash
# 전체 테스트
./gradlew test

# 모듈별 테스트
./gradlew :api:test
./gradlew :crawler:test
./gradlew :common:test
```

## 라이센스

MIT
