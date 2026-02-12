# Vercel 배포 가이드

## 사전 준비

- Node.js 18+
- Vercel 계정 (https://vercel.com)

## 1. Vercel CLI 설치

```bash
npm i -g vercel
```

## 2. 로그인

```bash
vercel login
```

브라우저가 열리면 계정 인증을 완료한다.

## 3. 배포

프로젝트 루트에서 실행:

```bash
vercel
```

| 질문 | 답변 |
|------|------|
| Set up and deploy? | `Y` |
| Which scope? | 본인 계정 선택 |
| Link to existing project? | `N` |
| Project name? | `bito-liquors` (또는 원하는 이름) |
| In which directory is your code located? | `./` (엔터) |
| Want to modify settings? | `N` |

Vite 프로젝트는 자동 감지되므로 빌드 설정을 별도로 수정할 필요 없다.

완료되면 프리뷰 URL이 출력된다.

## 4. 프로덕션 배포

```bash
vercel --prod
```

## 5. 이후 업데이트 배포

코드 수정 후 동일하게 실행:

```bash
vercel --prod
```

## 유용한 명령어

| 명령어 | 설명 |
|--------|------|
| `vercel ls` | 배포 목록 확인 |
| `vercel inspect <url>` | 배포 상세 정보 |
| `vercel env add` | 환경변수 추가 |
| `vercel logs <url>` | 배포 로그 확인 |
| `vercel rm <url>` | 배포 삭제 |
