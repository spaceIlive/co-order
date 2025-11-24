package gwan.co_order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "group_deliveries")
public class GroupDelivery {
    @Id @GeneratedValue
    @Column(name = "group_delivery_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Enumerated(EnumType.STRING)
    private GroupDeliveryStatus status;

    public static GroupDelivery createGroupDelivery(Post post) {
        GroupDelivery groupDelivery = new GroupDelivery();
        groupDelivery.post = post;
        groupDelivery.status = GroupDeliveryStatus.READY;
        return groupDelivery;
    }
}

