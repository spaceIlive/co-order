package gwan.co_order.repository;

import gwan.co_order.domain.Participation;
import gwan.co_order.domain.ParticipationStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ParticipationRepository {

    private final EntityManager em;

    public void saveParticipation(Participation participation) {
        em.persist(participation);
    }

    public void updateParticipation(Participation participation) {
        em.merge(participation);
    }

    public void deleteParticipation(Participation participation) {
        em.remove(participation);
    }

    public Participation findParticipation(Long participationId) {
        return em.find(Participation.class, participationId);
    }

    public List<Participation> findParticipationsByPostId(Long postId) {
        return em.createQuery("select p from Participation p where p.post.id = :postId", Participation.class)
                .setParameter("postId", postId)
                .getResultList();
    }

    // 특정 Post의 특정 상태 참여자 조회 (추가)
    public List<Participation> findParticipationsByPostIdAndStatus(Long postId, ParticipationStatus status) {
        return em.createQuery("select p from Participation p where p.post.id = :postId and p.status = :status", Participation.class)
                .setParameter("postId", postId)
                .setParameter("status", status)
                .getResultList();
    }

    public List<Participation> findParticipationsByMemberId(Long memberId) {
        return em.createQuery("select p from Participation p where p.member.id = :memberId", Participation.class)
                .setParameter("memberId", memberId)
                .getResultList();
    }

    // 특정 회원이 특정 모집글에 참여 중인지 확인
    public boolean existsByMemberIdAndPostId(Long memberId, Long postId) {
        Long count = em.createQuery(
            "select count(p) from Participation p " +
            "where p.member.id = :memberId " +
            "and p.post.id = :postId " +
            "and p.status = :status", 
            Long.class)
            .setParameter("memberId", memberId)
            .setParameter("postId", postId)
            .setParameter("status", ParticipationStatus.JOINED)
            .getSingleResult();
        
        return count > 0;
    }
}
