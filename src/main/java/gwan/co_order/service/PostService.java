package gwan.co_order.service;

import gwan.co_order.domain.*;
import gwan.co_order.policy.MinParticipantsPolicy;
import gwan.co_order.policy.PostScanRangePolicy;
import gwan.co_order.policy.ScanRange;
import gwan.co_order.repository.MemberRepository;
import gwan.co_order.repository.PostRepository;
import gwan.co_order.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;
    private final MinParticipantsPolicy minParticipantsPolicy;  // 최소 인원 계산 정책
    private final PostScanRangePolicy scanRangePolicy;  // 게시글 스캔 거리 정책

    public Post createPost(Long memberId, Long storeId, Address address, LocalDateTime deadline) {

        Member member = memberRepository.findMemberById(memberId);
        Store store = storeRepository.findStoreById(storeId);

        // 최소 인원 계산 (정책 인터페이스 활용)
        int minParticipants = minParticipantsPolicy.calculate(address, store.getAddress());

        Post post = Post.createPost(member, store, address, minParticipants, deadline);
        postRepository.savePost(post);
        
        return post;
    }

    @Transactional(readOnly = true)
    public List<Post> findNearOpenPosts(double latitude, double longitude) {
        // 정책으로부터 스캔 범위 가져오기
        ScanRange range = scanRangePolicy.getScanRange();
        
        return postRepository.findNearbyOpenPosts(
            latitude, 
            longitude, 
            range.getLatRange(), 
            range.getLngRange()
        );
    }

    // 마감 시간 도달 시 처리
    public void processExpiredOpenPosts(LocalDateTime currentTime) {
        List<Post> expiredPosts = postRepository.findExpiredOpenPost(currentTime);
        for (Post post : expiredPosts) {
            if (post.getCurrentParticipants() < post.getMinParticipants()) {
                postRepository.updatePostStatus(post, PostStatus.CANCELLED);
                continue;
            }
            postRepository.updatePostStatus(post, PostStatus.WAITING_ORDER);
        }
    }

    @Transactional(readOnly = true)
    public Post findPostById(Long postId) {
        return postRepository.findPostById(postId);
    }

    public void deletePost(Long postId) {
        Post post = postRepository.findPostById(postId);
        postRepository.delete(post);
    }
}
