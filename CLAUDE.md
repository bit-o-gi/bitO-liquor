# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

주류 가격 비교 서비스 모노레포. 한국 이커머스 사이트(롯데온, 이마트)에서 주류 가격을 크롤링하고, 가격 비교/추적 기능을 REST API와 웹 프론트엔드로 제공합니다.

## 모노레포 구조

```
liquor/
├── backend/          — Spring Boot 3.5.6 멀티모듈 (Java 21)
│   ├── common/       — 공통 라이브러리 (엔티티, 리포지토리, DTO)
│   ├── api/          — 조회/검색 API 앱 (:8080)
│   ├── crawler/      — 크롤링 앱 (:8081)
│   ├── build.gradle
│   ├── settings.gradle
│   └── gradlew
├── frontend/         — React + Vite 웹 앱
│   ├── src/
│   ├── public/
│   └── package.json
├── docs/
├── CLAUDE.md
└── README.md
```

## 빌드 및 개발 명령어

### Backend

```bash
cd backend
./gradlew build              # 전체 모듈 빌드
./gradlew :api:bootRun       # API 앱 실행 (포트 8080)
./gradlew :crawler:bootRun   # 크롤러 앱 실행 (포트 8081)
./gradlew test               # 전체 테스트 실행
./gradlew :api:test          # API 모듈 테스트만 실행
./gradlew clean              # 클린
```

### Frontend

```bash
cd frontend
npm install                  # 의존성 설치
npm run dev                  # Vite 개발 서버 실행 (HMR 지원)
npm run build                # tsc -b로 타입 체크 후 Vite 빌드
npm run lint                 # ESLint로 프로젝트 전체 린트
npm run preview              # 프로덕션 빌드 로컬 미리보기
```

## 사전 요구사항

- Java 21
- Node.js (npm)
- Chrome 브라우저 설치 (Selenium 4.x가 ChromeDriver 자동 관리)
- PostgreSQL 데이터베이스 (Supabase) — 환경변수 또는 `backend/.env`로 설정:
  - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`

## 아키텍처

### Backend

#### common 모듈 (`backend/common/src/main/java/org/bito/liquor/common/`)

공통 도메인 라이브러리로, `api`와 `crawler` 모두에서 의존합니다.

- `dto/` — LiquorDto (응답 매핑)
- `model/` — JPA 엔티티: Liquor, PriceHistory, Whisky
- `repository/` — LiquorRepository, PriceHistoryRepository, WhiskyRepository

#### api 모듈 (`backend/api/src/main/java/org/bito/liquor/`)

조회/검색 전용 API 앱 (포트 8080, `ApiApplication`).

- `config/` — WebConfig (CORS: localhost:5173 허용)
- `controller/` — LiquorController, WhiskyController
- `service/` — LiquorQueryService, WhiskyQueryService

#### crawler 모듈 (`backend/crawler/src/main/java/org/bito/liquor/`)

크롤링 전용 앱 (포트 8081, `CrawlerApplication`).

- `controller/` — LotteonCrawlController, EmartCrawlController
- `scraper/` — LotteonScraper, EmartScraper
- `service/` — LotteonCrawlService, EmartCrawlService

#### 주요 설계 패턴

- **스크래핑 전략**: 두 스크래퍼 모두 헤드리스 Chrome을 사용하며 봇 탐지 회피 설정 적용(커스텀 User-Agent, 자동화 플래그 비활성화). LotteonScraper는 `data-item` 속성에서 JSON 추출을 먼저 시도하고, 실패 시 DOM 파싱으로 폴백. EmartScraper는 스크롤 시뮬레이션과 함께 검색 결과를 페이지네이션하며 크롤링.
- **Upsert 방식**: 서비스에서 `productCode`로 상품을 매칭 — 기존 상품은 업데이트하고 가격 변동 시 이력을 저장, 신규 상품은 새로 삽입.
- **브랜드/카테고리 추출**: 상품명에서 정규식과 알려진 브랜드 목록을 사용하여 브랜드, 용량, 도수, 카테고리 등 메타데이터를 파싱. EmartScraper는 누락된 도수/용량에 대해 카테고리별 기본값도 적용.
- **두 가지 엔티티 모델**: `Liquor`(범용, 두 스크래퍼 모두 사용)와 `Whisky`(Supabase `public.whisky` 테이블, WhiskyQueryService를 통한 읽기 전용 조회).

### Frontend

React 19 + TypeScript (~5.9) + Vite 7 기반 단일 페이지 앱.

- **기술 스택**: Tailwind CSS v4 (`@tailwindcss/vite` 플러그인), ESLint
- `src/types/liquor.ts` — 핵심 데이터 타입: `Liquor`, `GroupedLiquor`
- `src/components/LiquorGrid.tsx` — `product_code` 기준으로 주류 그룹화 및 그리드 렌더링
- `src/components/LiquorCard.tsx` — 개별 상품 및 판매처별 가격 비교 표시
- `src/App.tsx` — 최상위 레이아웃

데이터 흐름: 목 데이터 → LiquorGrid에서 `product_code` 기준 그룹화 → 그룹별 LiquorCard 렌더링

## API 엔드포인트

### api 모듈 (`:8080`) — 조회/검색

**주류** (`/api/liquors`):
- `GET /` — 전체 조회
- `GET /{id}` — 단건 조회
- `GET /search?q=` — 검색
- `GET /brand/{brand}` — 브랜드별 조회
- `GET /category/{category}` — 카테고리별 조회
- `GET /cheap?max=` — 저렴한 주류 조회
- `GET /{id}/history` — 가격 이력 조회

**위스키** (`/api/whisky`):
- `GET /lowest` — 전체 소스에서 최저가 위스키 조회

### crawler 모듈 (`:8081`) — 크롤링 실행

- `POST /api/crawl/lotteon` — 롯데온 크롤링 실행
- `POST /api/crawl/emart` — 이마트 크롤링 실행

## 데이터베이스

- Supabase를 통한 PostgreSQL 사용
- api 모듈: JPA ddl-auto `update` (스키마 자동 관리)
- crawler 모듈: JPA ddl-auto `validate` (스키마 검증만)
- HikariCP `prepareThreshold=0` 설정 (Supabase 커넥션 풀러 호환용)
