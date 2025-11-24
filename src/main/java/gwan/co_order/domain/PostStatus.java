package gwan.co_order.domain;

public enum PostStatus {
    OPEN,       // 모집 중
    WAITING_ORDER,   // 오더 기다리는중
    ORDERED,    // 주문 완료
    CANCELLED   // 취소됨
}