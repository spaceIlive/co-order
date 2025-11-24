package gwan.co_order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "participation_products")
@Getter @Setter
public class ParticipationProduct {
    @Id @GeneratedValue
    @Column(name = "participation_product_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participation_id")
    private Participation participation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;
    private int unitPrice;

    public static ParticipationProduct createParticipationProduct(Participation participation, Product product, int quantity, int unitPrice) {
        ParticipationProduct participationProduct = new ParticipationProduct();
        participationProduct.participation = participation;
        participationProduct.product = product;
        participationProduct.quantity = quantity;
        participationProduct.unitPrice = unitPrice;
        return participationProduct;
    }
}
