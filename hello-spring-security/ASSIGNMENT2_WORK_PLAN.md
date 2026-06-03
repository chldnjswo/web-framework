# 과제2 Spring Boot 구현 순서 및 커밋 계획

이 문서는 `/Users/user/Downloads/과제2-SpringBoot (1).docx` 요구사항을 기준으로, 현재 프로젝트에서 어떤 순서로 구현하면 좋은지 정리한 작업 계획이다. 과제에서 최소 5회 이상 커밋을 요구하므로, 기능 단위로 7~8회 커밋하는 흐름을 권장한다.

## 1. 과제 핵심 요구사항 요약

- 모든 화면 하단 또는 상단에 실습 수행 일시, 학번, 성명을 표시한다.
- `/products` 상품 목록에 Spring Data JPA `Pageable`, `Page<Product>`, `@Query` 기반 페이징과 검색을 적용한다.
- ADMIN 전용 상품 수정 기능을 추가한다.
- 로그인 사용자용 비밀번호 변경 기능을 추가한다.
- 모든 기능 화면을 캡처하고, 날짜/시간/학번/성명이 스크린샷에 포함되게 한다.
- GitHub 커밋은 최소 5회 이상, 메시지는 `feat:`, `fix:`, `test:`, `refactor:`, `docs:` 접두사를 사용한다.
- 보고서는 PDF로 제출하며, GitHub URL, Security 필터 체인 다이어그램, 코드 분석, 실행 화면, 자기평가체크리스트를 포함한다.

## 2. 현재 코드 기준으로 먼저 확인한 상태

- 상품 목록은 현재 `productService.findAll()`로 전체 상품을 가져온다.
- `ProductRepository`에는 페이징/검색용 `@Query` 메서드가 아직 없다.
- 상품 등록/삭제는 있지만 `/products/{id}/edit` 상품 수정 기능은 없다.
- 비밀번호 변경용 `PasswordChangeDto`, `UserController`, `user/password.html`은 아직 없다.
- 공통 footer 또는 header에 실습 수행 일시/학번/성명을 출력하는 구조가 아직 없다.
- `DataInitializer`의 샘플 상품은 현재 4건만 들어가 있다. 과제 문서의 예시는 20건, 페이지당 5개, 총 4페이지 확인을 요구한다.

## 3. 권장 커밋 순서

### Commit 1: 기반 실행 증적 및 과제용 샘플 데이터 정리

권장 메시지:

```bash
git commit -m "feat: seed assignment sample products"
```

작업 내용:

- `DataInitializer`의 상품 샘플을 과제 문서 기준 20건으로 맞춘다.
- 관리자 계정은 기존처럼 `admin@hansung.ac.kr / admin1234`를 유지한다.
- 가능하면 일반 USER 테스트용 계정도 만들거나, 회원가입 화면으로 직접 생성할 계획을 세운다.

확인:

```bash
mvn test
```

스크린샷 후보:

- `/login`
- 관리자 로그인 후 `/home`
- `/products`에서 상품 데이터가 충분히 보이는지 확인

### Commit 2: 모든 화면에 실습 일시/학번/성명 표시

권장 메시지:

```bash
git commit -m "feat: add assignment footer timestamp"
```

작업 내용:

- `templates/fragments/assignment-footer.html` 같은 공통 footer fragment를 만든다.
- 모든 주요 Thymeleaf 화면에 footer fragment를 포함한다.
- 표시 예시:

```text
실습 수행 일시: 2026년 05월 29일 오후 04:30:12 | 학번: 본인학번 | 성명: 본인이름
```

구현 팁:

- 과제 문서가 "실시간" 표시를 요구하므로 JavaScript `setInterval()`로 1초마다 시간을 갱신하는 방식이 가장 단순하다.
- 학번/성명은 하드코딩해도 과제 목적상 충분하지만, 수정하기 쉽게 fragment 한 곳에만 둔다.
- 모든 스크린샷에 footer가 나오도록 페이지 최하단에만 두지 말고, 화면 높이가 길어도 캡처에 잡히는 위치를 고려한다.

확인:

- `/login`
- `/signup`
- `/home`
- `/products`
- `/products/add`
- `/admin/dashboard`

### Commit 3: 상품 페이징/검색 백엔드 구현

권장 메시지:

```bash
git commit -m "feat: add product paging search backend"
```

작업 내용:

- `ProductRepository`에 `@Query`와 `Pageable`을 받는 검색 메서드를 추가한다.
- `ProductService`에 `Page<Product>` 반환 메서드를 추가한다.
- `ProductController`의 `/products` 목록 조회를 `page`, `size`, `keyword` 파라미터 기반으로 변경한다.

주요 파일:

- `src/main/java/kr/ac/hansung/repository/ProductRepository.java`
- `src/main/java/kr/ac/hansung/service/ProductService.java`
- `src/main/java/kr/ac/hansung/controller/ProductController.java`

구현 방향:

```java
@Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword%")
Page<Product> findByNameContaining(@Param("keyword") String keyword, Pageable pageable);
```

```java
PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").ascending());
```

확인:

- `/products?page=0&size=5`
- `/products?page=1&size=5`
- `/products?keyword=삼성전자`
- `/products?keyword=애플`

### Commit 4: 상품 목록 화면에 검색 폼과 페이지 네비게이션 적용

권장 메시지:

```bash
git commit -m "feat: render product paging search ui"
```

작업 내용:

- `products/list.html`에서 `${products}` 대신 `${productPage.content}`를 사용한다.
- 총 상품 수, 현재 페이지, 전체 페이지를 표시한다.
- 검색 폼을 추가한다.
- 이전/페이지 번호/다음 버튼을 추가한다.
- 검색어가 있을 때 페이지 이동해도 `keyword` 파라미터가 유지되게 한다.

주요 파일:

- `src/main/resources/templates/products/list.html`

스크린샷 후보:

- 전체 상품 1페이지
- 전체 상품 2페이지
- `삼성전자` 검색 결과
- 검색 결과 없음 화면

### Commit 5: ADMIN 전용 상품 수정 기능 구현

권장 메시지:

```bash
git commit -m "feat: add admin product edit flow"
```

작업 내용:

- `ProductDto`에 검증 애너테이션을 추가한다.
- 필요하면 `spring-boot-starter-validation` 의존성을 `pom.xml`에 추가한다.
- `ProductService.updateProduct(Long id, ProductDto dto)`를 추가한다.
- `ProductController`에 `GET /products/{id}/edit`, `POST /products/{id}/edit`를 추가한다.
- `products/edit.html` 수정 폼을 만든다.
- `products/list.html` 관리 영역에 수정 버튼을 추가한다.
- `SecurityConfig`에 `/products/*/edit` ADMIN 권한을 추가한다.

주요 파일:

- `pom.xml`
- `src/main/java/kr/ac/hansung/dto/ProductDto.java`
- `src/main/java/kr/ac/hansung/service/ProductService.java`
- `src/main/java/kr/ac/hansung/controller/ProductController.java`
- `src/main/java/kr/ac/hansung/config/SecurityConfig.java`
- `src/main/resources/templates/products/edit.html`
- `src/main/resources/templates/products/list.html`

확인:

- ADMIN으로 `/products/{id}/edit` 접속 시 기존 데이터가 채워져 있는지 확인
- 수정 후 `/products`로 돌아오는지 확인
- 일반 USER로 `/products/{id}/edit` 접근 시 403이 나오는지 확인

스크린샷 후보:

- 상품 수정 폼
- 수정 완료 후 목록
- USER 접근 403 화면

### Commit 6: 로그인 사용자 비밀번호 변경 기능 구현

권장 메시지:

```bash
git commit -m "feat: add password change flow"
```

작업 내용:

- `PasswordChangeDto`를 추가한다.
- `UserService.changePassword()`를 추가한다.
- `UserController`를 새로 만들고 `GET /user/password`, `POST /user/password`를 구현한다.
- `user/password.html` 템플릿을 만든다.
- 홈 화면이나 navbar에 비밀번호 변경 링크를 추가한다.
- 현재 비밀번호 불일치, 새 비밀번호 확인 불일치, 성공 케이스를 처리한다.

주요 파일:

- `src/main/java/kr/ac/hansung/dto/PasswordChangeDto.java`
- `src/main/java/kr/ac/hansung/service/UserService.java`
- `src/main/java/kr/ac/hansung/controller/UserController.java`
- `src/main/resources/templates/user/password.html`
- 필요한 navbar가 있는 템플릿들

확인:

- 로그인 사용자로 `/user/password` 접속 가능
- 현재 비밀번호가 틀리면 오류 메시지 표시
- 새 비밀번호와 확인값이 다르면 오류 메시지 표시
- 변경 성공 후 새 비밀번호로 다시 로그인 가능

스크린샷 후보:

- 비밀번호 변경 폼
- 현재 비밀번호 불일치 오류
- 변경 완료 후 홈 화면

### Commit 7: 테스트 보강 및 기존 테스트 수정

권장 메시지:

```bash
git commit -m "test: cover assignment security flows"
```

작업 내용:

- 기존 `ProductControllerTest`는 `products` 모델을 기대하므로, 페이징 적용 후 `productPage` 기준으로 수정한다.
- 상품 수정 접근 권한 테스트를 추가한다.
- 비밀번호 변경 페이지 접근 테스트를 추가한다.
- 가능하면 `UserService.changePassword()` 단위 테스트 또는 통합 테스트를 추가한다.

확인:

```bash
mvn test
```

체크 포인트:

- 인증 사용자 상품 목록 200
- 비인증 사용자 상품 목록 로그인 리다이렉트
- USER 상품 수정 접근 403
- ADMIN 상품 수정 폼 200
- 로그인 사용자 비밀번호 변경 폼 200

### Commit 8: 보고서 자료 정리

권장 메시지:

```bash
git commit -m "docs: add assignment report checklist"
```

작업 내용:

- 보고서에 넣을 스크린샷 목록을 정리한다.
- 코드 분석에 넣을 핵심 코드 위치를 정리한다.
- Security 필터 체인 다이어그램 초안을 작성한다.
- 자기평가체크리스트를 보고서 마지막 페이지에 넣을 준비를 한다.

보고서 고정 순서:

1. 표지: 학번, 성명, GitHub Repository URL
2. 아키텍처: Spring Security 필터 체인 흐름 다이어그램
3. 코드 분석: Spring Data JPA 페이징, `@Query`, `Pageable`, `Page<T>`
4. 코드 분석: 상품 수정 Dirty Checking, 비밀번호 변경 BCrypt
5. 실행 화면 스크린샷
6. 마지막 페이지: 학습 소감 및 자기평가체크리스트

## 4. 커밋할 때 주의할 점

- 한 번에 모든 기능을 구현한 뒤 커밋하지 않는다.
- 각 커밋 전에 실제로 실행하거나 테스트한 내용을 메모한다.
- 커밋 메시지는 과제 문서 요구대로 접두사를 붙인다.
- GitHub에서 커밋 시간이 모두 같은 시각처럼 보이지 않게, 기능 구현과 확인을 한 단계씩 진행한 뒤 커밋한다.
- AI를 사용했다면 보고서의 학습 소감에 어떤 부분에서 도움을 받았고, 본인이 직접 확인한 내용은 무엇인지 솔직하게 적는다.

## 5. 구현 후 최종 확인 체크리스트

- [ ] `/login`, `/signup`, `/home`, `/products`, `/products/add`, `/admin/dashboard`에 날짜/시간/학번/성명 표시
- [ ] 상품 20건 기준 `/products?page=0&size=5`에서 5개씩 표시
- [ ] 페이지 번호, 이전, 다음 버튼 동작
- [ ] 검색어 입력 후 결과 표시
- [ ] 검색 상태에서 페이지 이동 시 검색어 유지
- [ ] 검색 결과 없음 화면 처리
- [ ] ADMIN 상품 수정 폼 접근 가능
- [ ] ADMIN 상품 수정 저장 가능
- [ ] USER 상품 수정 접근 시 403
- [ ] 로그인 사용자 비밀번호 변경 가능
- [ ] 현재 비밀번호 불일치 오류 표시
- [ ] 새 비밀번호 확인 불일치 오류 표시
- [ ] `mvn test` 통과
- [ ] GitHub 커밋 5회 이상
- [ ] 보고서 PDF에 GitHub URL, 다이어그램, 코드 분석, 스크린샷, 자기평가표 포함

## 6. 추천 작업 흐름 한 줄 요약

샘플 데이터 정리 → 공통 날짜/학번 footer → 상품 페이징/검색 백엔드 → 상품 목록 UI → ADMIN 상품 수정 → 비밀번호 변경 → 테스트/보고서 정리 순서로 진행하면, 과제 요구사항과 커밋 검증 기준을 자연스럽게 만족하기 좋다.
