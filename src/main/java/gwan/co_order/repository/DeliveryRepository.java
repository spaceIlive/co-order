package gwan.co_order.repository;

import gwan.co_order.domain.Delivery;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import gwan.co_order.domain.DeliveryStatus;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DeliveryRepository {

    private final EntityManager em;

    public void saveDelivery (Delivery delivery) {
        em.persist(delivery);
    }

    public Delivery findDeliveryById(Long deliveryId) {
        return em.find(Delivery.class, deliveryId);
    }

    public void updateDeliveryStatus(Long deliveryId, DeliveryStatus status) {
        Delivery delivery = findDeliveryById(deliveryId);
        delivery.setStatus(status);
        em.merge(delivery);
    }

    public List<Delivery> findDeliveriesByGroupDeliveryId(Long groupDeliveryId) {
        return em.createQuery("select d from Delivery d where d.groupDelivery.id = :groupId", Delivery.class)
                .setParameter("groupId", groupDeliveryId)
                .getResultList();
    }

    public void deleteDelivery(Delivery delivery) {
        em.remove(delivery);
    }
}
