package gwan.co_order.repository;

import gwan.co_order.domain.Order;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void saveOrder(Order order) {
        em.persist(order);
    }

    public Order findOrderById(Long orderId) {
        return em.find(Order.class, orderId);
    }

    public List<Order> findOrdersByMemberId(Long memberId) {
        return em.createQuery("select o from Order o where o.member.id = :memberId", Order.class)
                .setParameter("memberId", memberId)
                .getResultList();
    }

    public List<Order> findOrdersByGroupDeliveryId(Long groupDeliveryId) {
        return em.createQuery(
                        "select o from Order o " +
                                "join fetch o.member " +
                                "join fetch o.delivery d " +
                                "where d.groupDelivery.id = :groupId", Order.class)
                .setParameter("groupId", groupDeliveryId)
                .getResultList();
    }
}
