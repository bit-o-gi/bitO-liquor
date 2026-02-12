# Liquor Backend API 명세

> 프론트엔드 개발 시 참조용 API 문서입니다.
> 백엔드 서버: Spring Boot 3.5.6 / Java 21
> Swagger UI: http://localhost:8080/swagger-ui.html

## Base URL

| 환경 | URL |
|------|-----|
| 개발 | `http://localhost:8080/api` |

---

## 1. 주류 API (`/api/liquors`)

### 1.1 전체 조회

```
GET /api/liquors
```

- 응답: `LiquorDto[]` (updatedAt 기준 최신순 정렬)

### 1.2 단건 조회

```
GET /api/liquors/{id}
```

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `id` | Long (path) | 주류 ID |

- 응답: `LiquorDto`
- 없을 경우: `404 Not Found`

### 1.3 검색

```
GET /api/liquors/search?q={keyword}
```

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `q` | String (query, 필수) | 검색 키워드 (상품명 대소문자 무시 검색) |

- 응답: `LiquorDto[]`

### 1.4 브랜드별 조회

```
GET /api/liquors/brand/{brand}
```

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `brand` | String (path) | 브랜드명 |

- 응답: `LiquorDto[]`

### 1.5 카테고리별 조회

```
GET /api/liquors/category/{category}
```

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `category` | String (path) | 카테고리명 |

- 응답: `LiquorDto[]`

### 1.6 저렴한 주류 조회

```
GET /api/liquors/cheap?max={maxPrice}
```

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `max` | Integer (query, 기본값: 50000) | 최대 가격 (원) |

- 응답: `LiquorDto[]` (가격 오름차순 정렬)

### 1.7 가격 이력 조회

```
GET /api/liquors/{id}/history
```

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `id` | Long (path) | 주류 ID |

- 응답: `PriceHistory[]` (recordedAt 기준 최신순 정렬)

---

## 2. 위스키 API (`/api/whisky`)

### 2.1 최저가 위스키 조회

```
GET /api/whisky/lowest
```

- 응답: `Whisky[]`
- 동일 상품(name + class + volume)에 대해 소스(LOTTEON, EMART, TRADERS)별 최저가만 반환

---

## 응답 타입 정의

### LiquorDto

```typescript
interface LiquorDto {
  id: number;
  productCode: string;     // 고유 상품 코드
  name: string;            // 상품명
  brand: string | null;    // 브랜드
  category: string | null; // 카테고리 (위스키, 와인, 맥주 등)
  volume: number | null;   // 용량 (ml)
  alcoholPercent: number | null; // 도수 (%)
  country: string | null;  // 원산지
  currentPrice: number;    // 현재 판매가 (원)
  originalPrice: number | null;  // 원래 가격 (원)
  discountPercent: number | null; // 할인율 (%, 계산값)
  imageUrl: string | null; // 상품 이미지 URL
  productUrl: string | null; // 쇼핑몰 상품 링크
  source: string;          // 출처 ("LOTTEON" | "EMART")
  updatedAt: string;       // 최종 업데이트 (ISO 8601)
}
```

### PriceHistoryDto

```typescript
interface PriceHistoryDto {
  id: number;
  price: number;           // 기록된 가격 (원)
  recordedAt: string;      // 기록 시각 (ISO 8601)
}
```

### Whisky

```typescript
interface Whisky {
  id: number;
  name: string;            // 상품명
  class: string | null;    // 등급/클래스
  volume: number | null;   // 용량 (ml)
  brand: string | null;    // 브랜드
  category: string | null; // 카테고리
  country: string | null;  // 원산지
  alcoholPercent: number | null; // 도수 (%)
  currentPrice: number;    // 현재 판매가 (원)
  originalPrice: number | null;  // 원래 가격 (원)
  imageUrl: string | null; // 이미지 URL
  fullname: string | null; // 표시용 전체 이름
  productCode: string;     // 상품 코드
  productUrl: string | null; // 쇼핑몰 링크
  source: string;          // 출처 ("LOTTEON" | "EMART" | "TRADERS")
  createdAt: string;       // 생성일 (ISO 8601)
  updatedAt: string;       // 수정일 (ISO 8601)
}
```

---

## CORS 설정

- 허용 Origin: `http://localhost:5173`
- 허용 Methods: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`
- Credentials: 허용
- Preflight 캐시: 3600초

---

## 에러 응답

현재 글로벌 에러 핸들러 없음. 기본 Spring Boot 에러 응답 형식:

```json
{
  "timestamp": "2026-02-11T14:00:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "path": "/api/liquors/999"
}
```
