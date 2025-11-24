package gwan.co_order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "deliveries")
public class Delivery {
    @Id @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_delivery_id")
    private GroupDelivery groupDelivery;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    public static Delivery createDelivery(GroupDelivery groupDelivery, Address address) {
        Delivery delivery = new Delivery();
        delivery.groupDelivery = groupDelivery;
        delivery.address = address;
        delivery.status = DeliveryStatus.READY;
        return delivery;
    }
}
