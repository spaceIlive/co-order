# Co-Order (공동 주문 프로젝트)

## 기능 목록

### 회원 기능 (Member)

#### 회원 등록
- 이름, 비밀번호, 배송 정보(주소, latitude, longitude) 입력받아 등록
- 이름 중복 체크
- 비밀번호 확인 검증 (MemberForm에서 처리)

#### 로그인/로그아웃
- 이름과 비밀번호로 로그인
- 세션 기반 인증 (HttpSession 사용)
- 에러 메시지 구분:
  - 등록되지 않은 아이디
  - 비밀번호 불일치

---

### 모집글 기능 (Post)

#### 모집글 작성
- 호스트가 작성 (Host Member 지정)
- 가게 선택, 마감 시간 등록
- **마감 시간 검증**: 현재 시간보다 미래여야 함 (@Future)
- 호스트 위치 정보 저장
- 최소 인원은 시스템이 자동 계산 (MinParticipantsPolicy 사용)
- **호스트 자동 참여**: 모집글 작성 시 호스트의 Participation 자동 생성
- **호스트 상품 선택**: 모집글 작성 시 호스트도 상품 선택 필수
- Ajax 기반 동적 상품 로딩 (가게 선택 시)
- 에러 처리: try-catch로 예외 발생 시 사용자 친화적 메시지 표시

#### 모집글 삭제
- 참여자가 0명이 되면 자동 삭제
- Post 삭제 전 관련 Participation 및 ParticipationProduct 모두 삭제 (외래키 제약 조건 해결)

#### 모집글 조회
- 반경 내의 모집글 목록 확인 (현재 사용자 위치 기준)
- 검색 범위는 PostScanRangePolicy로 결정 (기본 1km)

#### 모집글 상세 조회
- 현재 참여 인원 수, 최소 필요 인원
- 가게 정보, 마감 시간
- 현재 1인당 배달비 (참여 인원이 늘수록 감소)
- 지금 참여하면 줄어드는 배달비 표시

#### 모집글 상태 관리 (자동)
- OPEN: 모집 중
- WAITING_ORDER: 마감 후 최소 인원 충족, 주문 생성 대기 중
- ORDERED: 주문 생성 완료
- CANCELLED: 마감 후 최소 인원 미달로 취소됨

---

### 참여 기능 (Participation & ParticipationProduct)

#### 모집 참여
- 마감 전인 Post에 참여 (JOINED 상태로 등록)
- 참여 시 회원의 배송지 정보를 스냅샷으로 저장
- Post의 참여 인원 수 자동 증가
- **중복 참여 방지**: 같은 회원이 같은 모집글에 중복 참여 불가
- **호스트 자동 참여**: 모집글 작성 시 호스트의 Participation 자동 생성

#### 참여 시 메뉴 선택 (ParticipationProduct)
- 참여자가 주문할 상품(Product)과 수량 선택
- 각 상품의 가격과 수량으로 총액 계산
- 실시간 가격 계산 (JavaScript)
- Ajax 기반 상품 목록 로딩

#### 참여 취소
- **"내 주문" 페이지에서 취소 버튼 제공** (모집중인 주문만)
- 마감 전까지만 취소 가능 (CANCELLED 처리)
- Post의 참여 인원 수 감소
- 취소 시 ParticipationProduct 자동 삭제
- 참여 인원이 0명이 되면 Post 자동 삭제
- 에러 발생 시 세션 기반 에러 메시지 표시

#### 참여 현황 조회
- 특정 Post의 참여자 목록 확인
- 각 참여자의 주문 예정 상품 목록 확인
- **내 참여 목록**: 회원별 참여 목록을 상태별로 분류 조회
  - 모집중 (Post.status = OPEN)
  - 완료됨 (Post.status = ORDERED 또는 WAITING_ORDER)
  - 취소됨 (Post.status = CANCELLED)

---

### 가게 기능 (Store)

#### 가게 등록
- 이름, 가게 주소(주소, latitude, longitude) 입력

#### 가게 조회
- 가게 목록 및 상세 정보 확인

---

### 상품 기능 (Product)

#### 메뉴 등록
- 가게별 메뉴 등록 (가게 선택, 이름, 가격)
- Product는 Store를 참조 (어느 가게의 상품인지)
- **도메인 규칙**: 같은 가게 내에서 상품 이름 중복 불가

#### 메뉴 수정
- 가격, 이름 변경
- 이름 변경 시 중복 체크 (같은 가게 내)

#### 메뉴 조회
- 특정 가게의 전체 메뉴 목록 확인
- Ajax API로 상품 목록 반환 (ProductDto 사용)

---

### 주문 기능 (Order & OrderProduct)

#### 주문 생성 (서버에서 자동)
- Post 마감 시간 도달 시 시스템이 자동 처리 (Scheduler)
- 최소 인원 이상 모였을 때: JOINED 상태의 각 Participation마다 Order 1개 생성
- Order는 개인별로 생성됨 (1명 = 1개 Order)
- **주문 생성 프로세스** (OrderService의 메서드 분리):
  1. `createOrdersFromExpiredPosts()`: WAITING_ORDER 상태 Post들 조회
  2. `processPostOrders()`: 각 Post 처리
     - **GroupDelivery 생성** (Post당 1개, 같은 배달원 배정을 위한 그룹)
     - 참여자들 순회
  3. `createOrderForParticipation()`: 개별 Order 생성
     - Delivery 생성 (GroupDelivery 참조 + 개별 배송지 스냅샷)
     - Order 생성 (member, store, delivery)
  4. `createOrderProducts()`: OrderProduct들 생성 및 가격 계산
     - ParticipationProduct → OrderProduct 변환
     - unitPrice 스냅샷 (주문 당시 개당 가격)
     - Order.updateTotalPrice()로 총액 누적
- ParticipationProduct 정보를 OrderProduct로 스냅샷 변환
  - 상품명, 가격은 주문 당시 값으로 고정 (unitPrice)
  - Product 엔티티는 참조로 유지 (나중에 상품 정보 추적 가능)

#### 주문 조회
- 개인별 주문 내역 확인
- 주문한 가게, 총 금액 확인
- 주문 상품 목록 (OrderProduct) 확인

#### 배송 관리 (GroupDelivery & Delivery)
- **그룹 배달 구조**:
  - Post 1개 → GroupDelivery 1개 (같은 배달원 배정)
  - GroupDelivery 1개 → Delivery N개 (개별 배송지)
  - Delivery 1개 → Order 1개 (1:1 관계)
- **GroupDelivery**: 그룹 배달 정보
  - Post와 1:1 관계
  - 배달원 정보 (나중에 추가 가능)
  - 그룹 전체 배송 상태 (READY/IN_PROGRESS/COMPLETED)
- **Delivery**: 개별 배송 정보
  - GroupDelivery 참조 (ManyToOne)
  - 개별 배송지 주소 (Participation에서 스냅샷한 Address)
  - 개별 배송 상태 (READY/COMP)
- **장점**: 같은 Post의 모든 Order가 한 배달원에게 배정되면서도, 각자 다른 배송지 유지

---

## 핵심 설계 사항

### 최소 인원 정책
- 최소 인원은 사용자가 입력하는 값이 아님
- 시스템 내부 정책(MinParticipantsPolicy)에 의해 자동으로 계산됨
- 현재 구현: DeliveryFeeBasedMinParticipantsPolicy
  - 호스트 위치 ↔ 가게 위치 거리 계산 (Haversine 공식)
  - 총 배달비 = 기본 3000원 + (거리 × 500원/km)
  - 최소 인원 = 총 배달비 / 1000원 (올림)

### 배달비 분담 구조
- 총 배달비는 거리에 따른 비용
- 참여 인원이 늘어날수록 1인당 배달비 감소
- 모집글 상세 조회 시 현재 인당 배달비를 실시간으로 표시 및 지금 참여하면 얼마나 배달비가 줄어드는지

### 마감 시점 자동 처리 (Scheduler)
- **PostClosureScheduler**: 10초마다 자동 실행
- **1단계**: PostService.processExpiredOpenPosts()
  - 마감 시간 도달한 OPEN 상태 Post 조회
  - 최소 인원 충족 여부 확인
    - 충족: Post 상태 → WAITING_ORDER
    - 미달: Post 상태 → CANCELLED
- **2단계**: OrderService.createOrdersFromExpiredPosts()
  - WAITING_ORDER 상태 Post 조회
  - 각 Post마다:
    - **GroupDelivery 생성** (Post당 1개)
    - JOINED 상태의 각 Participation마다 Order 생성
      - Delivery 생성 (GroupDelivery 참조 + 배송지 스냅샷)
      - OrderProduct들 생성 (상품/가격 스냅샷)
      - Order의 totalPrice 자동 계산
  - Post 상태 → ORDERED

### 참여자 관리
- 모든 참여자가 취소하여 참여자가 0명이 되면 모집글 자동 삭제

### 스냅샷 패턴
- Participation: Member의 배송지를 참여 시점에 스냅샷 저장
- Delivery: Participation의 배송지를 주문 확정 시점에 스냅샷 저장
- OrderProduct: Product의 가격을 unitPrice로 저장 (주문 시점 개당 가격 고정)
- 이후 원본 데이터가 변경되어도 과거 주문/참여 내역은 불변

### 기술적 요구사항
- 모든 관계는 단방향 (자식 → 부모 참조)
- Address는 값 타입(Embeddable)으로 관리 (주소, latitude, longitude)
- 정책(Policy) 패턴 적용
  - 최소 인원 계산 정책 (MinParticipantsPolicy)
  - 게시글 스캔 범위 정책 (PostScanRangePolicy)
  - 인터페이스 기반으로 쉽게 교체 가능한 설계
- **Form 객체 패턴**: 폼 데이터 바인딩 및 검증
  - MemberForm, ProductForm, PostForm, ParticipationForm, StoreForm
  - @Valid, @NotEmpty, @NotNull, @AssertTrue 검증
- **ProductDto**: Ajax API 응답용 (JavaScript JSON 변환)
- **Thymeleaf 템플릿**: Fragments로 공통 부분 재사용
- **Bootstrap CSS**: 반응형 UI
- **세션 관리**: HttpSession 기반 로그인 상태 유지

---

### 정책(Policy) 구조

#### 최소 인원 계산 정책
- **MinParticipantsPolicy**: 최소 필요 인원 계산 인터페이스
- **DeliveryFeeBasedMinParticipantsPolicy**: 배달비 기반 구현체
  - 거리(Haversine 공식) 기반 배달비 계산
  - 기본 배달비 3000원 + km당 500원
  - 목표 1인당 배달비 1000원 기준으로 최소 인원 산출

#### 게시글 스캔 범위 정책
- **PostScanRangePolicy**: 주변 게시글 검색 범위 인터페이스
- **FixedRadiusScanRangePolicy**: 고정 반경 구현체
  - 기본 1km 반경 검색
  - ScanRange DTO로 위도/경도 범위 반환

---

### 아키텍처 구조

#### 계층 구조
```
┌───────────────────────────────────────┐
│          View 계층                     │
│  - Thymeleaf Templates                │
│    (index, login, post-list, etc.)    │
│  - Fragments (header, footer)         │
│  - JavaScript (Ajax, 실시간 계산)        │
└───────────────┬───────────────────────┘
                ↓
┌───────────────────────────────────────┐
│        Controller 계층                 │
│  - HomeController                     │
│  - MemberController                   │
│  - PostController                     │
│  - ParticipationController            │
│  - OrderController                    │
│  - StoreController                    │
│  - ProductController                  │
└───────────────┬───────────────────────┘
                ↓
┌──────────────────────────────────────┐
│         Scheduler 계층                │
│  - PostClosureScheduler (자동 실행)    │
└───────────────┬──────────────────────┘
                ↓
┌───────────────────────────────────────┐
│          Service 계층                  │
│  - PostService                        │
│  - OrderService                       │
│  - ParticipationService               │
│  - MemberService                      │
│  - StoreService                       │
│  - ProductService                     │
│  - DeliveryService                    │
└───────────────┬───────────────────────┘
                ↓
┌───────────────────────────────────────┐
│        Repository 계층                 │
│  - PostRepository                     │
│  - OrderRepository                    │
│  - ParticipationRepository            │
│  - MemberRepository                   │
│  - StoreRepository                    │
│  - ProductRepository                  │
│  - GroupDeliveryRepository            │
│  - DeliveryRepository                 │
│  - OrderProductRepository             │
│  - ParticipationProductRepository     │
└───────────────┬───────────────────────┘
                ↓
┌───────────────────────────────────────┐
│          Domain 계층                   │
│  - Post, Order, Participation         │
│  - Member, Store, Product             │
│  - GroupDelivery, Delivery            │
│  - OrderProduct, ParticipationProduct │
│  - Address (값 타입)                    │
└───────────────────────────────────────┘

┌──────────────────────────────────────┐
│          Policy 계층                  │
│  - MinParticipantsPolicy             │
│  - PostScanRangePolicy               │
└──────────────────────────────────────┘

**참고**: Form 객체(MemberForm, PostForm 등)는 폼 데이터 바인딩 및 검증에 사용되며, ProductDto는 Ajax API 응답용으로만 사용됩니다.
```

#### 주요 설계 원칙
1. **단방향 참조**: 자식 → 부모만 참조, 양방향 참조 지양
2. **계층 분리**: View → Controller → Scheduler → Service → Repository → Domain
3. **정책 패턴**: 비즈니스 규칙을 인터페이스로 추상화
4. **메서드 분리**: 복잡한 로직은 단일 책임 원칙에 따라 분리
5. **스냅샷 패턴**: 생성시점 데이터 보존 (Address, unitPrice)
6. **Form 객체 패턴**: 폼 데이터 바인딩 및 검증 (MemberForm, PostForm 등)

---

### 구현 현황

#### Repository 계층
- ✅ MemberRepository
  - 회원 조회 (ID, 이름)
  - 로그인용 회원 조회 (예외 처리 포함)
- ✅ StoreRepository
- ✅ ProductRepository
  - 상품 조회 (ID, 가게별)
  - 같은 가게 내 상품 이름 중복 체크
- ✅ PostRepository
  - 주변 모집글 조회 (위도/경도 범위)
  - FETCH JOIN으로 Store 함께 조회
- ✅ ParticipationRepository
  - 참여 조회 (ID, Post별, 회원별)
  - 중복 참여 체크 (회원ID + PostID)
- ✅ ParticipationProductRepository
- ✅ OrderRepository
- ✅ OrderProductRepository
- ✅ GroupDeliveryRepository
- ✅ DeliveryRepository

#### Service 계층
- ✅ MemberService
  - 회원 등록 (이름 중복 체크)
  - 로그인 (ID/비밀번호 검증, 예외 메시지 구분)
- ✅ StoreService (가게 등록/조회)
- ✅ ProductService
  - 메뉴 등록 (이름 중복 체크)
  - 메뉴 수정 (이름 변경 시 중복 체크)
  - 메뉴 조회/삭제
- ✅ PostService
  - 모집글 작성 (호스트 자동 참여 포함)
  - 주변 모집글 조회
  - 마감 처리
- ✅ ParticipationService
  - 참여 생성 (중복 참여 방지, 호스트 자동 참여 제외)
  - 참여 취소
  - 상품 추가
  - 내 참여 목록 조회 (상태별 그룹화: 모집중/완료됨/취소됨)
- ✅ OrderService (주문 생성/조회)
- ✅ DeliveryService (배송 관리)

#### Controller 계층
- ✅ **HomeController**
  - `GET /`: 메인 대시보드 (세션 체크, 미로그인 시 리다이렉트)

- ✅ **MemberController**
  - `GET /login`: 로그인 폼
  - `POST /login`: 로그인 처리 (에러 메시지 구분: notfound/wrongpassword)
  - `GET /logout`: 로그아웃 (세션 무효화)
  - `GET /members/new`: 회원가입 폼
  - `POST /members`: 회원가입 처리 (MemberForm, 비밀번호 확인 검증, 이름 중복 체크)

- ✅ **StoreController**
  - `GET /stores`: 가게 목록 조회
  - `GET /stores/new`: 가게 등록 폼
  - `POST /stores`: 가게 등록 처리 (StoreForm)

- ✅ **ProductController**
  - `GET /stores/{storeId}/products`: 가게별 상품 목록 조회
  - `GET /stores/{storeId}/products/new`: 상품 등록 폼
  - `POST /products`: 상품 등록 처리 (ProductForm, 이름 중복 체크)
  - `GET /products/{productId}/edit`: 상품 수정 폼 (ProductForm.from() 사용)
  - `POST /products/{productId}/edit`: 상품 수정 처리 (이름 변경 시 중복 체크)

- ✅ **PostController**
  - `GET /posts`: 주변 모집글 목록 조회 (현재 사용자 위치 기준)
  - `GET /posts/{postId}`: 모집글 상세 조회
  - `GET /posts/new`: 모집글 작성 폼 (가게 목록 제공)
  - `POST /posts`: 모집글 작성 처리 (PostForm, 호스트 자동 참여, 상품 선택 필수)
  - `GET /api/stores/{storeId}/products`: Ajax 상품 목록 API (ProductDto 반환)

- ✅ **ParticipationController**
  - `GET /posts/{postId}/participations/new`: 참여 폼 (상품 목록, 예상 배달비)
  - `POST /posts/{postId}/participations`: 참여 생성 (중복 참여 방지, 상품 선택 필수)
  - `POST /participations/{participationId}/cancel`: 참여 취소 (세션 기반 에러 메시지)

- ✅ **OrderController**
  - `GET /orders`: 내 주문 조회 (상태별 분류: 모집중/완료됨/취소됨, 세션 에러 메시지 표시)

#### Form 객체 및 DTO
- ✅ **Form 객체**: 폼 데이터 바인딩 및 검증
  - ProductForm (상품 등록/수정, 검증 포함)
  - ParticipationForm (참여 상품 선택)
  - PostForm (모집글 작성)
  - StoreForm (가게 등록)
  - MemberForm (회원가입, 비밀번호 확인 검증)
- ✅ **ProductDto**: Ajax API 응답용 (JavaScript JSON 변환)

#### View 계층 (Thymeleaf)
- ✅ Fragments (header, bodyHeader, footer)
- ✅ index.html (메인 대시보드)
- ✅ login.html (로그인)
- ✅ member-register.html (회원가입)
- ✅ post-list.html (모집글 목록)
- ✅ post-register.html (모집글 작성, Ajax 상품 로딩)
- ✅ participation-register.html (참여하기, 상품 선택)
- ✅ product-list.html (상품 목록)
- ✅ product-register.html (상품 등록)
- ✅ product-edit.html (상품 수정)
- ✅ my-orders.html (내 주문 조회)

#### Policy 계층
- ✅ MinParticipantsPolicy (최소 인원 계산)
  - DeliveryFeeBasedMinParticipantsPolicy (배달비 기반 구현체)
- ✅ PostScanRangePolicy (스캔 범위 결정)
  - FixedRadiusScanRangePolicy (고정 반경 구현체)

#### Scheduler 계층
- ✅ PostClosureScheduler (마감 자동 처리, 1분마다 실행)

#### 주요 기능 구현
- ✅ 세션 기반 로그인/로그아웃
- ✅ 모집글 작성 시 호스트 자동 참여
- ✅ 중복 참여 방지 (같은 회원이 같은 모집글에 중복 참여 불가)
- ✅ 상품 이름 중복 방지 (같은 가게 내)
- ✅ Ajax 기반 동적 상품 로딩
- ✅ 실시간 가격 계산 (JavaScript)
- ✅ 에러 메시지 표시 (중복 참여, 상품 미선택 등)
- ✅ 내 주문 조회 (모집중/완료됨/취소됨 분류, 취소는 접어두기)

---

## 도메인 모델 구조

### 엔티티 목록

#### 모집 단계
- **Post**: 공동 주문 모집글
  - host(Member), store(Store), address(Address 스냅샷)
  - minParticipants, currentParticipants, deadline, status
  
- **Participation**: 모집글 참여 정보
  - member(Member), post(Post), address(Address 스냅샷)
  - status (JOINED/CANCELLED)
  
- **ParticipationProduct**: 참여자별 주문 예정 상품
  - participation(Participation), product(Product)
  - quantity, totalPrice

#### 주문 단계
- **Order**: 개인별 주문 (1명당 1개)
  - member(Member), store(Store), delivery(Delivery)
  - totalPrice
  - `updateTotalPrice(unitPrice, quantity)`: 총액 누적 계산 메서드

- **GroupDelivery**: 그룹 배달 정보 (Post당 1개)
  - post(Post) - 1:1 관계
  - status (READY/IN_PROGRESS/COMPLETED)
  - 같은 Post의 모든 Order를 한 배달원에게 배정하기 위한 그룹
  
- **Delivery**: 개별 배송 정보 (Order당 1개)
  - groupDelivery(GroupDelivery) - ManyToOne
  - address(Address 스냅샷)
  - status (READY/COMP)
  
- **OrderProduct**: 주문 상품 상세
  - order(Order), product(Product)
  - unitPrice (주문 시점 개당 가격 스냅샷), quantity

#### 기본 엔티티
- **Member**: 회원 (name, address)
- **Store**: 가게 (name, address)
- **Product**: 상품 (store, name, price)

#### 값 타입
- **Address**: 주소 정보 (address, latitude, longitude)

### 주요 관계
- Post → Member(host), Store (ManyToOne)
- Participation → Post, Member (ManyToOne)
- ParticipationProduct → Participation, Product (ManyToOne)
- GroupDelivery → Post (OneToOne)
- Delivery → GroupDelivery (ManyToOne)
- Order → Member, Store, Delivery (ManyToOne, OneToOne)
- OrderProduct → Order, Product (ManyToOne)
- Product → Store (ManyToOne)

### 데이터 흐름
```
1. Post 생성 (OPEN)
   - 호스트의 Participation 자동 생성
   ↓
2. 다른 회원들의 Participation 생성 (JOINED)
   - ParticipationProduct들 선택
   ↓
3. 마감 시간 도달 (Scheduler 자동 실행)
   - PostService: 최소 인원 체크
     - 충족: OPEN → WAITING_ORDER
     - 미달: OPEN → CANCELLED
   ↓
4. 주문 생성 (Scheduler 자동 실행)
   - OrderService: WAITING_ORDER 상태 Post 처리
     - Post마다:
       * GroupDelivery 생성 (Post당 1개)
     - 각 Participation마다:
       * Delivery 생성 (GroupDelivery 참조 + 배송지 스냅샷)
       * Order 생성 (member, store, delivery)
       * OrderProduct들 생성 (상품/가격 스냅샷)
       * totalPrice 자동 계산
     - Post 상태: WAITING_ORDER → ORDERED

배달 구조:
Post 1개
  ↓
GroupDelivery 1개 (배달원 배정)
  ↓
Delivery N개 (각자 배송지)
  ↓
Order N개 (개별 주문)
```

---

## 프로젝트 실행 방법

### 필수 요구사항

1. **Java 17 이상**
   - JDK 17 설치 및 `java -version`으로 확인

2. **Gradle Wrapper**
   - 저장소에 `gradlew`/`gradlew.bat` 포함
   - 별도 Gradle 설치 없이 Wrapper 스크립트 사용

3. **H2 Database**
   - 애플리케이션은 `jdbc:h2:tcp://localhost/~/jpashop`에 연결하므로 H2 실행이 필요함
   - 설치 및 실행 절차 (한 번만 설치)
     1. [H2 Download 페이지](https://www.h2database.com/html/download.html) → *Platform-Independent Zip* 다운로드
     2. 압축 해제 후 `bin` 폴더로 이동 (예: `cd ~/h2/bin`)
     3. 실행 스크립트 실행  
        - Mac/Linux: `./h2.sh`  
        - Windows: `h2.bat`
     4. 실행된 H2 콘솔 창을 **닫지 말고** 둔 채 Spring Boot 애플리케이션 실행
   - H2를 끄면 애플리케이션이 DB에 연결하지 못하므로 항상 켠 상태를 유지

### 실행 단계

#### 1. 프로젝트 클론
```bash
git clone https://github.com/spaceIlive/co-order.git
cd co-order
```

#### 2. 프로젝트 빌드
```bash
# Mac/Linux
./gradlew build

# Windows
gradlew.bat build
```

#### 3. 애플리케이션 실행
```bash
# Mac/Linux
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

또는 IDE에서 실행:
- `CoOrderApplication.java` 파일을 찾아 실행
- 또는 IDE의 Spring Boot 실행 기능 사용

#### 4. 접속
- 브라우저에서 `http://localhost:8080` 접속
- 로그인 페이지가 표시됩니다

### 초기 데이터

프로젝트 실행 시 `data.sql`이 자동으로 실행되어 다음 데이터가 생성됩니다 (상세 좌표는 `src/main/resources/data.sql` 참고):

- **회원**: 10명 (윤관 + 회원1~회원9) – 대부분 (1.0, 1.0) 주변 좌표
  - 로그인 정보 예시: 이름 "윤관", 비밀번호 "123"
- **가게**: 20개
  - 1km 이내: 10개
  - 1km 이외: 10개
- **상품**: 각 가게당 5개씩 총 100개

#### 좌표 기준 설명
- 모든 좌표는 (1.0, 1.0)을 기준으로 ±0.01 정도의 범위를 사용
- `PostController`는 로그인한 회원 위치에서 **약 1km 반경**(`PostScanRangePolicy` 기본값)만 조회
  - 위도/경도 약 0.009° 차이가 대략 1km
  - 예: 회원 위치 (1.0, 1.0)일 때 (1.008, 0.992)는 조회되지만 (1.05, 1.05)는 제외
- 실제 테스트 시 가게/회원 좌표를 조금만 변경해도 목록 노출 여부가 달라지니 주의

### 데이터베이스 접속

H2 데이터베이스 콘솔 접속:
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:tcp://localhost/~/jpashop`
- 사용자명: `sa`
- 비밀번호: (공백)
- H2 콘솔 프로그램(`./h2.sh` 또는 `h2.bat`)을 실행해 둔 상태여야 접속 가능

### 주의사항

1. **포트 충돌**
   - 포트 8080이 이미 사용 중이면 `application.yaml`에서 포트 변경
   ```yaml
   server:
     port: 8081
   ```

2. **데이터 초기화**
   - 애플리케이션 재시작 시 `ddl-auto: create` 설정으로 인해 모든 데이터가 삭제되고 재생성됩니다
   - 테스트용 초기 데이터는 `data.sql`에서 자동으로 삭제됩니다

3. **테스트 실행**
   ```bash
   # Mac/Linux
   ./gradlew test

   # Windows
   gradlew.bat test
   ```

