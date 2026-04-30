# ChickenWiki

ChickenWiki는 여러 치킨 브랜드의 메뉴 정보를 한곳에서 확인하고, 메뉴별 리뷰와 평점을 남길 수 있는 치킨 메뉴 리뷰 서비스입니다.

프론트엔드는 React와 Vite로 구성했고, 백엔드는 Spring Boot와 Spring Data JPA 기반으로 구현했습니다. BBQ, BHC, 교촌치킨, 굽네치킨 등 브랜드 공식 홈페이지의 메뉴 데이터를 크롤링해 PostgreSQL에 저장하고, 저장된 메뉴 데이터는 리뷰, 태그, 정렬, 추천 기능과 연결됩니다.

## Contributors

| 이름 | GitHub | 주요 담당 |
| --- | --- | --- |
| 이진우 | [JinwooLeeDev](https://github.com/JinwooLeeDev) | 전체 프론트엔드, API 연동, 주요 서비스 기능 구현 |
| 김주형 | [jkim00781-netizen](https://github.com/jkim00781-netizen) | 브랜드별 메뉴 크롤링, 데이터 동기화, 백엔드/DB 구조 보완 |

## Overview

- 브랜드 목록 조회
- 브랜드별 메뉴 목록 및 상세 정보 조회
- 메뉴명 기반 검색
- 메뉴별 리뷰 작성, 수정, 삭제
- 리뷰 추천 및 추천 수 기반 정렬
- 최신순, 평점순 리뷰 정렬
- 리뷰 수, 평균 평점 기반 브랜드별 메뉴 정렬
- 회원가입, 로그인, 마이페이지
- 관리자 권한 기반 리뷰 삭제, 사용자 정보 조회, 계정 삭제
- 브랜드별 메뉴 수동 크롤링 및 전체 브랜드 크롤링 API

## Tech Stack

### Frontend

- React 19
- Vite
- React Router DOM
- ESLint

### Backend

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Supabase
- H2 Console
- Jsoup

### Tools

- Git
- GitHub

## Project Structure

```text
ChickenWiki/
├─ frontend/                         # React + Vite frontend
│  ├─ src/
│  │  ├─ components/                 # 주요 페이지와 UI 컴포넌트
│  │  ├─ context/                    # 인증 상태 관리
│  │  └─ services/                   # API 호출 로직
│  ├─ public/
│  └─ package.json
├─ src/main/java/com/ChickenWiki/ChickenWiki/
│  ├─ domain/
│  │  ├─ auth/                       # 인증 API
│  │  ├─ brand/                      # 브랜드, 메뉴, 태그
│  │  ├─ crawling/                   # 브랜드별 크롤러와 동기화
│  │  ├─ menu/                       # 메뉴 조회 API
│  │  ├─ review/                     # 리뷰와 추천
│  │  └─ user/                       # 사용자, 마이페이지, 관리자 기능
│  └─ global/                        # 공통 설정
├─ src/main/resources/
│  └─ application.properties
├─ build.gradle
├─ dev.ps1
├─ dev.bat
└─ README.md
```

## Main Pages

- `/` : 메인 페이지, 메뉴 검색
- `/brands` : 브랜드 목록
- `/brand/:id` : 브랜드 상세 및 메뉴 목록
- `/menu/:id` : 메뉴 상세, 리뷰 목록, 리뷰 작성/수정/삭제
- `/login` : 로그인 및 회원가입
- `/mypage` : 내 정보와 내가 작성한 리뷰
- `/admin/users/:nickname` : 관리자용 사용자 상세 페이지

## Core Features

### Frontend & API

- React Router 기반 페이지 라우팅 구성
- 메인 페이지 메뉴 검색 및 메뉴 상세 페이지 이동 흐름 구현
- 브랜드 목록, 브랜드 상세, 메뉴 상세, 리뷰, 사용자 API 연동
- 로그인 상태를 전역 컨텍스트로 관리하고 인증 토큰 기반 요청 처리
- 리뷰 작성, 수정, 삭제 UI와 API 흐름 구현
- 리뷰 추천 수, 최신순, 평점순 정렬 기능 구현
- 브랜드별 메뉴를 리뷰 수와 평균 평점 기준으로 정렬
- 관리자 권한에 따라 리뷰 삭제, 사용자 조회, 계정 삭제 기능 제공

### Crawling & Data Sync

- BBQ, BHC, 교촌치킨, 굽네치킨 공식 홈페이지의 메뉴 수집 구조 분석
- 브랜드마다 다른 페이지 구조와 응답 방식을 고려해 크롤러를 분리 구현
- 메뉴명, 가격, 설명, 이미지 URL, 브랜드 고유 메뉴 ID(`source_menu_id`) 수집
- 서비스에서 바로 활용할 수 있도록 수집 데이터를 메뉴 엔티티와 태그 구조에 맞게 정리
- 재크롤링 시 `source_menu_id`를 기준으로 기존 메뉴와 신규 메뉴를 매핑
- 전체 삭제 후 재삽입 방식이 아니라 기존 데이터와 신규 데이터를 비교해 필요한 데이터만 추가 또는 업데이트
- 기존 메뉴 ID를 최대한 유지해 리뷰, 수동 태그, 정렬 데이터가 끊기지 않도록 구성
- 더 이상 수집되지 않는 메뉴는 삭제하지 않고 비활성화해 서비스 데이터의 연속성 확보
- 브랜드별 필요한 메뉴 영역과 엔드포인트만 요청하도록 구성해 불필요한 접근 최소화
- 수동 실행 중심의 크롤링 API와 요청 간격 설정으로 대상 서버 부하를 고려

## API Summary

### Auth

- `POST /api/auth/signup`
- `POST /api/auth/login`
- `GET /api/auth/me`

### User

- `POST /api/users/signup`
- `POST /api/users/login`
- `GET /api/users/me`
- `GET /api/users/admin/by-nickname/{nickname}`
- `DELETE /api/users/admin/by-nickname/{nickname}`

### Brand

- `GET /api/brands`
- `GET /api/brands/{id}`
- `GET /api/brands/{id}/menus`
- `GET /api/brands/{id}/reviews`

### Menu

- `GET /api/menus`
- `GET /api/menus/{menuId}`

### Review

- `GET /api/menus/{menuId}/reviews`
- `POST /api/menus/{menuId}/reviews`
- `PUT /api/menus/{menuId}/reviews/{reviewId}`
- `DELETE /api/menus/{menuId}/reviews/{reviewId}`
- `POST /api/menus/{menuId}/reviews/{reviewId}/recommend`

### Crawling

- `GET /api/crawl/all`
- `GET /api/crawl/bbq`
- `GET /api/crawl/bhc`
- `GET /api/crawl/kyochon`
- `GET /api/crawl/goobne`

## Example Request

리뷰 생성 요청 예시:

```json
{
  "content": "바삭하고 매콤해서 맛있어요.",
  "rating": 5
}
```

인증이 필요한 요청은 `Authorization: Bearer <token>` 헤더를 사용합니다.

크롤링 API는 `CRAWL_ADMIN_TOKEN`이 설정된 경우 `X-Crawl-Token` 헤더가 필요합니다.

## Environment Variables

백엔드 실행 전 아래 환경 변수를 준비해야 합니다.

```bash
DB_PASSWORD=your_postgres_password
JWT_SECRET=your_jwt_secret
JWT_TOKEN_VALID_HOURS=12
CRAWL_ADMIN_TOKEN=optional_admin_token
```

설정 파일:

- [application.properties](src/main/resources/application.properties)

## How To Run

### Backend

프로젝트 루트에서 실행합니다.

```bash
./gradlew bootRun
```

Windows:

```powershell
.\gradlew.bat bootRun
```

기본 주소:

```text
http://localhost:8080
```

### Frontend

`frontend` 디렉터리에서 실행합니다.

```bash
npm install
npm run dev
```

기본 주소:

```text
http://localhost:5173
```

Vite 개발 서버는 `/api` 요청을 백엔드 서버로 프록시합니다.

설정 파일:

- [vite.config.js](frontend/vite.config.js)

## Build

### Frontend

```bash
cd frontend
npm run build
```

### Backend

```bash
./gradlew build
```

## Implementation Points

- 단순 메뉴 수집이 아니라 실제 서비스에서 리뷰, 태그, 정렬과 연결될 수 있는 데이터 구조로 설계했습니다.
- 브랜드마다 다른 공식 홈페이지 구조를 분석해 브랜드별 맞춤 크롤러를 구성했습니다.
- 재크롤링 시 기존 리뷰와 수동 관리 데이터가 사라지지 않도록 고유 메뉴 ID 기반 동기화 방식을 적용했습니다.
- 사용자 경험 측면에서는 검색, 상세 조회, 리뷰 작성, 추천, 정렬, 마이페이지, 관리자 기능까지 하나의 흐름으로 연결했습니다.
- Supabase PostgreSQL과 Spring Data JPA를 연동해 메뉴, 리뷰, 추천, 태그, 사용자 데이터를 관리합니다.

## Notes

- Java 21이 필요합니다.
- 현재 백엔드는 `spring.jpa.hibernate.ddl-auto=none`으로 설정되어 있어 테이블을 자동 생성하지 않습니다.
- PostgreSQL 연결을 위해 `DB_PASSWORD` 환경 변수가 필요합니다.
- 크롤링 요청 간격은 `app.crawling.minimum-interval-days`와 `app.crawling.request-delay-ms` 설정으로 조절합니다.
