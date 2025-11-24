package gwan.co_order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order {
    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 어느 가게에 넣는 주문인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private int totalPrice;

    // === 생성 메서드 ===
    public static Order createOrder(Member member, Store store, Delivery delivery) {
        Order order = new Order();
        order.member = member;
        order.store = store;
        order.delivery = delivery;
        order.totalPrice = 0;
        return order;
    }
    //해당 프로덕트 가격이랑 수량 곱해서 totalprice에 더해줌
    public void updateTotalPrice(int productPrice, int quantity) {
        this.totalPrice += productPrice * quantity;
    }
}
