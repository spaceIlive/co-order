package gwan.co_order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "order_products")
@Getter @Setter
public class OrderProduct {
    @Id @GeneratedValue
    @Column(name = "order_product_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private int unitPrice;
    private int quantity;

    // === 생성 메서드 ===
    public static OrderProduct createOrderProduct(Order order, Product product, int unitPrice, int quantity) {
        OrderProduct orderProduct = new OrderProduct();
        orderProduct.order = order;
        orderProduct.product = product;
        orderProduct.unitPrice = unitPrice;
        orderProduct.quantity = quantity;
        return orderProduct;
    }
}
