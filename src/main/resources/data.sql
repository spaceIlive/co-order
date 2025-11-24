-- 초기 데이터 설정 SQL
-- 가게: 위도경도 1,1 기준으로 1km 이내와 이외 반반씩
-- 각 가게에 상품도 포함

-- ============================================
-- 회원 데이터
-- ============================================
INSERT INTO members (member_id, name, password, address, latitude, longitude) VALUES
(1, '윤관', '123', '신촌', 1, 1),
(2, '회원1', '123', '홍대', 1.002, 1.003),
(3, '회원2', '123', '강남', 0.998, 0.997),
(4, '회원3', '123', '잠실', 1.01, 1.01),
(5, '회원4', '123', '송파', 0.99, 1.02),
(6, '회원5', '123', '신촌', 1, 1),
(7, '회원6', '123', '홍대', 1.002, 1.003),
(8, '회원7', '123', '강남', 0.998, 0.997),
(9, '회원8', '123', '잠실', 1.01, 1.01),
(10, '회원9', '123', '송파', 0.99, 1.02);

-- ============================================
-- 가게 데이터 (1km 이내 - 10개)
-- ============================================
INSERT INTO stores (store_id, name, address, latitude, longitude) VALUES
(1, '맛있는 치킨집', '서울시 강남구 테헤란로 123', 1.005, 1.003),
(2, '피자나라', '서울시 강남구 역삼동 456', 0.995, 1.007),
(3, '중화요리', '서울시 강남구 논현동 789', 1.008, 0.992),
(4, '떡볶이 전문점', '서울시 강남구 선릉로 321', 0.992, 1.005),
(5, '햄버거킹', '서울시 강남구 삼성로 654', 1.002, 1.001),
(11, '돈까스 전문점', '서울시 강남구 역삼로 100', 1.004, 1.002),
(12, '라면집', '서울시 강남구 테헤란로 200', 0.996, 1.006),
(13, '족발보쌈', '서울시 강남구 논현로 300', 1.007, 0.993),
(14, '삼겹살집', '서울시 강남구 선릉로 400', 0.993, 1.004),
(15, '초밥집', '서울시 강남구 삼성로 500', 1.003, 1.002);

-- ============================================
-- 가게 데이터 (1km 이외 - 10개)
-- ============================================
INSERT INTO stores (store_id, name, address, latitude, longitude) VALUES
(6, '멀리있는 파스타집', '서울시 서초구 서초대로 111', 1.05, 1.05),
(7, '먼곳 피자', '서울시 송파구 올림픽로 222', 0.95, 0.95),
(8, '원거리 중식당', '서울시 강동구 천호대로 333', 1.08, 0.92),
(9, '떨어져있는 일식집', '서울시 영등포구 여의대로 444', 0.92, 1.08),
(10, '먼곳 치킨', '서울시 마포구 홍대로 555', 1.1, 1.1),
(16, '먼곳 돈까스', '서울시 서초구 서초대로 222', 1.06, 1.06),
(17, '원거리 라면', '서울시 송파구 올림픽로 333', 0.94, 0.94),
(18, '멀리 족발집', '서울시 강동구 천호대로 444', 1.09, 0.91),
(19, '떨어진 삼겹살', '서울시 영등포구 여의대로 555', 0.91, 1.09),
(20, '먼 초밥집', '서울시 마포구 홍대로 666', 1.11, 1.11);


-- ============================================
-- 상품 데이터 (1km 이내 가게들)
-- ============================================
-- 맛있는 치킨집 (store_id = 1)
INSERT INTO products (product_id, store_id, name, price) VALUES
(1, 1, '후라이드 치킨', 18000),
(2, 1, '양념 치킨', 19000),
(3, 1, '간장 치킨', 19000),
(4, 1, '치킨무', 0),
(5, 1, '콜라', 2000);

-- 피자나라 (store_id = 2)
INSERT INTO products (product_id, store_id, name, price) VALUES
(6, 2, '페퍼로니 피자', 20000),
(7, 2, '콤비네이션 피자', 22000),
(8, 2, '하와이안 피자', 23000),
(9, 2, '치즈 피자', 18000),
(10, 2, '피클', 1000);

-- 중화요리 (store_id = 3)
INSERT INTO products (product_id, store_id, name, price) VALUES
(11, 3, '짜장면', 6000),
(12, 3, '짬뽕', 7000),
(13, 3, '탕수육', 15000),
(14, 3, '양장피', 20000),
(15, 3, '군만두', 5000);

-- 떡볶이 전문점 (store_id = 4)
INSERT INTO products (product_id, store_id, name, price) VALUES
(16, 4, '떡볶이', 4000),
(17, 4, '순대', 5000),
(18, 4, '어묵', 3000),
(19, 4, '튀김', 4000),
(20, 4, '오뎅', 3000);

-- 햄버거킹 (store_id = 5)
INSERT INTO products (product_id, store_id, name, price) VALUES
(21, 5, '불고기 버거', 5500),
(22, 5, '치즈 버거', 5000),
(23, 5, '새우 버거', 6000),
(24, 5, '감자튀김', 3000),
(25, 5, '콜라', 2000);

-- 돈까스 전문점 (store_id = 11)
INSERT INTO products (product_id, store_id, name, price) VALUES
(51, 11, '돈까스 정식', 9000),
(52, 11, '치즈돈까스', 11000),
(53, 11, '왕돈까스', 13000),
(54, 11, '우동', 6000),
(55, 11, '미소시루', 3000);

-- 라면집 (store_id = 12)
INSERT INTO products (product_id, store_id, name, price) VALUES
(56, 12, '라면', 5000),
(57, 12, '치즈라면', 6000),
(58, 12, '떡라면', 6500),
(59, 12, '만두라면', 7000),
(60, 12, '공기밥', 1000);

-- 족발보쌈 (store_id = 13)
INSERT INTO products (product_id, store_id, name, price) VALUES
(61, 13, '족발 (소)', 25000),
(62, 13, '족발 (중)', 35000),
(63, 13, '족발 (대)', 45000),
(64, 13, '보쌈', 20000),
(65, 13, '막국수', 8000);

-- 삼겹살집 (store_id = 14)
INSERT INTO products (product_id, store_id, name, price) VALUES
(66, 14, '삼겹살 (1인분)', 12000),
(67, 14, '목살 (1인분)', 13000),
(68, 14, '갈비살 (1인분)', 15000),
(69, 14, '된장찌개', 7000),
(70, 14, '공기밥', 1000);

-- 초밥집 (store_id = 15)
INSERT INTO products (product_id, store_id, name, price) VALUES
(71, 15, '연어초밥 (6pcs)', 12000),
(72, 15, '참치초밥 (6pcs)', 14000),
(73, 15, '모둠초밥 (12pcs)', 20000),
(74, 15, '우동', 6000),
(75, 15, '미소시루', 2000);

-- ============================================
-- 상품 데이터 (1km 이외 가게들)
-- ============================================
-- 멀리있는 파스타집 (store_id = 6)
INSERT INTO products (product_id, store_id, name, price) VALUES
(26, 6, '크림 파스타', 12000),
(27, 6, '토마토 파스타', 11000),
(28, 6, '알리오 올리오', 10000),
(29, 6, '까르보나라', 13000),
(30, 6, '샐러드', 8000);

-- 먼곳 피자 (store_id = 7)
INSERT INTO products (product_id, store_id, name, price) VALUES
(31, 7, '불고기 피자', 24000),
(32, 7, '고구마 피자', 25000),
(33, 7, '포테이토 피자', 23000),
(34, 7, '치즈 오븐 파스타', 15000),
(35, 7, '치킨 스틱', 12000);

-- 원거리 중식당 (store_id = 8)
INSERT INTO products (product_id, store_id, name, price) VALUES
(36, 8, '마파두부', 8000),
(37, 8, '깐풍기', 18000),
(38, 8, '유산슬', 20000),
(39, 8, '볶음밥', 7000),
(40, 8, '유린기', 17000);

-- 떨어져있는 일식집 (store_id = 9)
INSERT INTO products (product_id, store_id, name, price) VALUES
(41, 9, '연어 초밥', 15000),
(42, 9, '참치 초밥', 14000),
(43, 9, '우동', 8000),
(44, 9, '돈까스', 12000),
(45, 9, '라멘', 9000);

-- 먼곳 치킨 (store_id = 10)
INSERT INTO products (product_id, store_id, name, price) VALUES
(46, 10, '매운 치킨', 20000),
(47, 10, '허니 치킨', 19000),
(48, 10, '마늘 치킨', 20000),
(49, 10, '치킨 샐러드', 15000),
(50, 10, '치킨 버거', 8000);

-- 먼곳 돈까스 (store_id = 16)
INSERT INTO products (product_id, store_id, name, price) VALUES
(76, 16, '돈까스 정식', 9500),
(77, 16, '치즈돈까스', 11500),
(78, 16, '왕돈까스', 13500),
(79, 16, '우동', 6500),
(80, 16, '미소시루', 3000);

-- 원거리 라면 (store_id = 17)
INSERT INTO products (product_id, store_id, name, price) VALUES
(81, 17, '라면', 5500),
(82, 17, '치즈라면', 6500),
(83, 17, '떡라면', 7000),
(84, 17, '만두라면', 7500),
(85, 17, '공기밥', 1000);

-- 멀리 족발집 (store_id = 18)
INSERT INTO products (product_id, store_id, name, price) VALUES
(86, 18, '족발 (소)', 26000),
(87, 18, '족발 (중)', 36000),
(88, 18, '족발 (대)', 46000),
(89, 18, '보쌈', 21000),
(90, 18, '막국수', 8500);

-- 떨어진 삼겹살 (store_id = 19)
INSERT INTO products (product_id, store_id, name, price) VALUES
(91, 19, '삼겹살 (1인분)', 13000),
(92, 19, '목살 (1인분)', 14000),
(93, 19, '갈비살 (1인분)', 16000),
(94, 19, '된장찌개', 7500),
(95, 19, '공기밥', 1000);

-- 먼 초밥집 (store_id = 20)
INSERT INTO products (product_id, store_id, name, price) VALUES
(96, 20, '연어초밥 (6pcs)', 13000),
(97, 20, '참치초밥 (6pcs)', 15000),
(98, 20, '모둠초밥 (12pcs)', 21000),
(99, 20, '우동', 6500),
(100, 20, '미소시루', 2000);

-- 초기 데이터가 ID를 직접 지정하므로, 이후 JPA가 사용하는 시퀀스를 충분히 큰 값으로 재시작
CREATE SEQUENCE IF NOT EXISTS members_seq START WITH 1000;
ALTER SEQUENCE members_seq RESTART WITH 1000;

CREATE SEQUENCE IF NOT EXISTS stores_seq START WITH 1000;
ALTER SEQUENCE stores_seq RESTART WITH 1000;

CREATE SEQUENCE IF NOT EXISTS products_seq START WITH 1000;
ALTER SEQUENCE products_seq RESTART WITH 1000;

CREATE SEQUENCE IF NOT EXISTS posts_seq START WITH 1000;
ALTER SEQUENCE posts_seq RESTART WITH 1000;

CREATE SEQUENCE IF NOT EXISTS participations_seq START WITH 1000;
ALTER SEQUENCE participations_seq RESTART WITH 1000;

CREATE SEQUENCE IF NOT EXISTS participation_products_seq START WITH 1000;
ALTER SEQUENCE participation_products_seq RESTART WITH 1000;

CREATE SEQUENCE IF NOT EXISTS group_deliveries_seq START WITH 1000;
ALTER SEQUENCE group_deliveries_seq RESTART WITH 1000;

CREATE SEQUENCE IF NOT EXISTS deliveries_seq START WITH 1000;
ALTER SEQUENCE deliveries_seq RESTART WITH 1000;

CREATE SEQUENCE IF NOT EXISTS orders_seq START WITH 1000;
ALTER SEQUENCE orders_seq RESTART WITH 1000;

CREATE SEQUENCE IF NOT EXISTS order_products_seq START WITH 1000;
ALTER SEQUENCE order_products_seq RESTART WITH 1000;
