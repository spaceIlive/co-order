package gwan.co_order.repository;

import gwan.co_order.domain.GroupDelivery;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GroupDeliveryRepository {

    private final EntityManager em;

    public void save(GroupDelivery groupDelivery) {
        em.persist(groupDelivery);
    }

    public GroupDelivery findGroupDeliveryByGroupId(Long groupDeliveryId) {
        return em.find(GroupDelivery.class, groupDeliveryId);
    }

    public GroupDelivery findGroupDeliveryByPostId(Long postId) {
        return em.createQuery("select gd from GroupDelivery gd where gd.post.id = :postId", GroupDelivery.class)
                .setParameter("postId", postId)
                .getSingleResult();
    }

    public void deleteGroupDelivery(GroupDelivery groupDelivery) {
        em.remove(groupDelivery);
    }
}

