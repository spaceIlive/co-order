package gwan.co_order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter @Setter
public class Product {
    @Id @GeneratedValue
    @Column(name = "product_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    private String name;
    private int price;

    public static Product createProduct(Store store, String name, int price) {
        Product product = new Product();
        product.setStore(store);
        product.setName(name);
        product.setPrice(price);
        return product;
    }
}
