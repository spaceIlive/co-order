package gwan.co_order.repository;

import gwan.co_order.domain.OrderProduct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderProductRepository {

    private final EntityManager em;

    public void saveOrderProduct (OrderProduct orderProduct) {
        em.persist(orderProduct);
    }
}
