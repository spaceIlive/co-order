package gwan.co_order.repository;

import gwan.co_order.domain.Post;
import gwan.co_order.domain.PostStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class PostRepository {

    private final EntityManager em;

    //저장
    public void savePost(Post post) {
        em.persist(post);
    }

    // 단건 상세 조회
    public Post findPostById(Long postId) {
        return em.find(Post.class, postId);
    }

    // 주변 OPEN 모집글 조회
    public List<Post> findNearbyOpenPosts(double userLat, double userLng, double latRange, double lngRange) {
        double minLat = userLat - latRange;
        double maxLat = userLat + latRange;
        double minLng = userLng - lngRange;
        double maxLng = userLng + lngRange;

        return em.createQuery(
            "select p from Post p " +
            "where p.status = :status " +
            "and p.address.latitude between :minLat and :maxLat " +
            "and p.address.longitude between :minLng and :maxLng", 
            Post.class)
            .setParameter("status", PostStatus.OPEN)
            .setParameter("minLat", minLat)
            .setParameter("maxLat", maxLat)
            .setParameter("minLng", minLng)
            .setParameter("maxLng", maxLng)
            .getResultList();
    }

    //삭제
    public void delete(Post post) {
        em.remove(post);
    }

    //모집글 상태 변경
    public void updatePostStatus(Post post, PostStatus status) {
        post.setStatus(status);
        em.merge(post);
    }

    public List<Post> findExpiredOpenPost (LocalDateTime currentTime) {
        return em.createQuery("select p from Post p where p.status = :status and p.deadline < :currentTime", Post.class)
                .setParameter("status", PostStatus.OPEN)
                .setParameter("currentTime", currentTime)
                .getResultList();
    }

    public List<Post> findWaitingOrderPosts () {
        return em.createQuery("select p from Post p where p.status = :status", Post.class)
                .setParameter("status", PostStatus.WAITING_ORDER)
                .getResultList();
    }
}
