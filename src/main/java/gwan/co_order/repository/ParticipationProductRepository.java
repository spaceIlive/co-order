package gwan.co_order.repository;

import gwan.co_order.domain.ParticipationProduct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ParticipationProductRepository {

    private final EntityManager em;

    // 저장
    public void saveParticipationProduct(ParticipationProduct participationProduct) {
        em.persist(participationProduct);
    }

    // 단건 조회
    public ParticipationProduct findParticipationProductById(Long participationProductId) {
        return em.find(ParticipationProduct.class, participationProductId);
    }

    // 특정 참여의 모든 상품 조회
    public List<ParticipationProduct> findParticipationProductsById(Long participationId) {
        return em.createQuery(
            "select pp from ParticipationProduct pp where pp.participation.id = :participationId", 
            ParticipationProduct.class)
            .setParameter("participationId", participationId)
            .getResultList();
    }

    // 삭제
    public void deleteParticipationProduct(ParticipationProduct participationProduct) {
        em.remove(participationProduct);
    }
}

