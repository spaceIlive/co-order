package gwan.co_order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "stores")
@Getter @Setter
public class Store {
    @Id @GeneratedValue
    @Column(name = "store_id")
    private Long id;

    private String name;

    @Embedded
    private Address address;
}