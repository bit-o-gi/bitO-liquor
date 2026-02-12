# Repository Guidelines

## 프로젝트 구조 및 모듈 구성
- 멀티모듈 Gradle 프로젝트: `api`, `crawler`, `common`.
- `api`: 조회/검색 API 애플리케이션 (`api/src/main/java`, `api/src/main/resources`).
- `crawler`: 크롤링 애플리케이션 (`crawler/src/main/java`, `crawler/src/main/resources`).
- `common`: 공통 도메인/리포지토리/서비스 라이브러리 (`common/src/main/java`).
- 테스트: 모듈별 `*/src/test/java/` (JUnit/Spring Boot).
- 빌드 산출물: 각 모듈의 `*/build/` 및 루트 `build/` (Gradle 생성).

## 빌드, 테스트, 개발 명령어
- `./gradlew build`: 전체 모듈 컴파일과 테스트를 수행합니다.
- `./gradlew test`: 전체 모듈 테스트만 실행합니다.
- `./gradlew :api:bootRun`: API 앱을 실행합니다. 기본 주소는 `http://localhost:8080`입니다.
- `./gradlew :crawler:bootRun`: 크롤러 앱을 실행합니다.

Windows에서는 `gradlew.bat`을 사용하세요.

## 코딩 스타일 및 네이밍 규칙
- Java 21, Spring Boot 관례를 따릅니다.
- 들여쓰기: 스페이스 4칸, 탭 사용 금지.
- 패키지: 소문자 (`org.bito.liquor.controller`).
- 클래스: `UpperCamelCase` (예: `LotteonScraper`).
- 메서드/필드: `lowerCamelCase`.
- 별도 포매터는 없으니, 기존 코드 스타일과 import 정렬을 유지하세요.

## 테스트 가이드
- 프레임워크: Spring Boot Test (JUnit Platform).
- 테스트 클래스는 모듈별 `*/src/test/java/`에 두고 `*Tests` 접미사를 권장합니다 (예: `ApiApplicationTests`).
- 커버리지 기준은 없지만, 새 기능은 서비스 로직과 스크래퍼 로직을 중심으로 테스트를 추가하세요.

## 커밋 및 PR 가이드
- 커밋 메시지는 Conventional Commits를 따릅니다 (예: `feat: ...`, `feat(scraper): ...`).
- 작고 명확한 단위로 커밋하세요.
- PR에는 변경 요약, 관련 이슈 링크(있다면), 검증 방법을 포함하세요.
- API/스크래핑 변경 시 `curl` 예시나 로그 샘플을 첨부하면 좋습니다.

## 설정 및 보안 참고
- DB 설정은 모듈별 `application.properties`(`api/src/main/resources/application.properties`, `crawler/src/main/resources/application.properties`)에서 환경 변수로 읽습니다.
  - `DB_URL` (기본값: `jdbc:postgresql://localhost:5432/postgres`)
  - `DB_USERNAME` (기본값: `postgres`)
  - `DB_PASSWORD` (기본값: 빈 값)
- 스크래퍼는 Selenium/Chrome에 의존하므로 로컬에 Chrome이 설치되어 있어야 합니다.
