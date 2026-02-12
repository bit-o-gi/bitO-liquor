# Repository Guidelines

## 프로젝트 구조 및 모듈 구성
이 저장소는 백엔드와 프론트엔드를 함께 관리하는 모노레포입니다.
- `backend/`: Java 21, Spring Boot 멀티모듈 서비스
- `frontend/`: React 19 + TypeScript + Vite 웹 애플리케이션 (현재 mock 데이터 기반)

백엔드 모듈:
- `backend/common`: 공통 도메인 모델, DTO, Repository
- `backend/api`: 조회/검색 API 애플리케이션 (`:8080`)
- `backend/crawler`: 크롤링 실행 애플리케이션 (`:8081`)

프론트엔드 주요 경로:
- `frontend/src/components`: UI 컴포넌트
- `frontend/src/types`: 타입 정의
- `frontend/src/data`: 목 데이터
- `frontend/public/images`: 정적 이미지

데이터/도메인 참고:
- 핵심 상품 테이블은 `public.liquors`입니다.

## 빌드, 테스트, 개발 명령어
백엔드 (`backend/`에서 실행):
- `./gradlew build`: 전체 모듈 컴파일 + 테스트 실행
- `./gradlew test`: 전체 백엔드 테스트 실행
- `./gradlew :api:bootRun`: API 서버 실행
- `./gradlew :crawler:bootRun`: 크롤러 서버 실행

프론트엔드 (`frontend/`에서 실행):
- `npm install`: 의존성 설치
- `npm run dev`: Vite 개발 서버 실행
- `npm run build`: 타입체크(`tsc -b`) 후 프로덕션 빌드
- `npm run lint`: ESLint 실행
- `npm run preview`: 빌드 결과 로컬 미리보기

## 코딩 스타일 및 네이밍 규칙
- Java: 스페이스 4칸, 클래스 `UpperCamelCase`, 메서드/필드 `lowerCamelCase`, 패키지 소문자(`org.bito.liquor...`).
- TypeScript/React: 기존 파일 기준 2칸 들여쓰기, 컴포넌트 파일은 `PascalCase`(예: `LiquorCard.tsx`), 변수/함수는 `camelCase`.
- 새 규칙을 추가하기보다 기존 파일 스타일과 import 정렬을 우선 따릅니다.

## 테스트 가이드
- 백엔드 테스트는 `backend/*/src/test/java`에서 Spring Boot Test + JUnit Platform을 사용합니다.
- 테스트 클래스명은 `*Tests` 패턴을 권장합니다(예: `ApiApplicationTests`).
- PR 전 최소 `./gradlew test`를 실행하세요. 단, DB 환경변수/로컬 설정이 없으면 `contextLoads`가 실패할 수 있습니다.
- 프론트엔드는 현재 린트 중심입니다. 테스트를 추가하면 `src/` 근처에 배치하고 실행 명령을 `frontend/package.json`에 명시하세요.

## 커밋 및 PR 가이드
- 커밋 메시지는 Conventional Commits 형식을 사용하세요(현재 이력 예: `feat: ...`).
- API, 크롤러, 프론트 변경을 가능한 한 작은 단위로 분리해 커밋하세요.
- PR에는 다음을 포함하세요:
  - 변경 요약(사용자 영향/동작 변화)
  - 검증 방법(실행한 명령어)
  - 관련 이슈 링크(있다면)
  - 프론트 UI 변경 시 스크린샷 또는 GIF

## 보안 및 설정 팁
- 비밀값은 커밋하지 말고 환경변수(`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`)를 사용하세요.
- 로컬 개발 시 `backend/api/src/main/resources/application-local.properties.example` 및 `backend/crawler/src/main/resources/application-local.properties.example`를 복사해 `application-local.properties`를 만드세요.
- 크롤러는 Selenium/Chrome 의존성이 있으므로 크롤러 관련 변경 전 로컬 Chrome 환경을 확인하세요.
