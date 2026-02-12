# 프론트엔드 개발 환경 세팅 가이드

## 1. 프론트엔드 레포 생성

```bash
cd ~/IdeaProjects
npm create vite@latest liquor-frontend -- --template react-ts
cd liquor-frontend
npm install
npm install axios
```

## 2. CLAUDE.md 작성

프론트엔드 레포 루트에 `CLAUDE.md` 생성:

```markdown
# liquor-frontend

React + TypeScript + Tailwind CSS 프론트엔드.
주류 가격 비교/추적 서비스의 클라이언트 앱.

## 백엔드 API 명세

/Users/parkseryu/IdeaProjects/liquor-backend/docs/api-spec.md 참고

## 개발 서버

- 프론트엔드: http://localhost:5173
- 백엔드 API: http://localhost:8080/api
```

## 3. Claude Code 실행

```bash
cd ~/IdeaProjects/liquor-frontend
claude
```

이후 "주류 목록 페이지 만들어줘", "검색 기능 추가해줘" 등 요청하면
Claude가 `api-spec.md`를 읽고 API 구조를 파악해서 개발 진행.
