package gwan.co_order.repository;

import gwan.co_order.domain.Address;
import gwan.co_order.domain.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class MemberRepository {
    @PersistenceContext
    private EntityManager em;

    public void saveMember(Member member) {
        em.persist(member);
    }

    public boolean existsByName(String name) {
        return em.createQuery("select count(m) from Member m where m.name = :name", Long.class)
                .setParameter("name", name)
                .getSingleResult() > 0;
    }

    public Member findMemberById(Long memberId) {
        return em.find(Member.class, memberId);
    }

    public Member findMemberByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getSingleResult();
    }

    

    public Address findAddress(Long memberId) {
        return em.createQuery("select m.address from Member m where m.id = :id", Address.class)
                .setParameter("id", memberId)
                .getSingleResult();
    }

    public void updateAddress(Long memberId, Address address) {
        Member member = em.find(Member.class, memberId);
        member.setAddress(address);
        em.merge(member);
    }

    // 로그인용 - 결과 없을 수 있음
    public Member findMemberByNameForLogin(String name) {
        try {
            return em.createQuery("select m from Member m where m.name = :name", Member.class)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (Exception e) {
            return null;  // 해당 이름의 회원이 없으면 null 반환
        }
    }
}
