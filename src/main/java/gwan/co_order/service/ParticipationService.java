package gwan.co_order.service;

import gwan.co_order.domain.*;
import gwan.co_order.repository.MemberRepository;
import gwan.co_order.repository.ParticipationProductRepository;
import gwan.co_order.repository.ParticipationRepository;
import gwan.co_order.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final ParticipationProductRepository participationProductRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    //모집 참여
    @Transactional
    public Long joinPost(Long memberId, Long postId, Address deliveryAddress) {
        Member member = memberRepository.findMemberById(memberId);
        Post post = postRepository.findPostById(postId);

        // 중복 참여 검증 (호스트도 이미 참여되어 있어서 여기서 걸림)
        if (participationRepository.existsByMemberIdAndPostId(memberId, postId)) {
            throw new IllegalStateException("이미 참여한 모집글입니다.");
        }

        validateDeadline(post);
        validatePostStatus(post);

        post.addParticipant();

        Participation participation = Participation.createParticipation(member, post, deliveryAddress);
        participationRepository.saveParticipation(participation);

        return participation.getId();
    }

    //참여 취소
    @Transactional
    public void cancelParticipation(Long participationId) {
        Participation participation = participationRepository.findParticipation(participationId);
        if (participation == null) {
            throw new IllegalStateException("참여 정보를 찾을 수 없습니다.");
        }
        
        Post post = participation.getPost();
        if (post == null) {
            throw new IllegalStateException("모집글 정보를 찾을 수 없습니다.");
        }

        validateDeadline(post);
        if (participation.getStatus() == ParticipationStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 참여입니다.");
        }

        List<ParticipationProduct> products = 
            participationProductRepository.findParticipationProductsById(participationId);
        
        for (ParticipationProduct product : products) {
            participationProductRepository.deleteParticipationProduct(product);
        }

        participation.setStatus(ParticipationStatus.CANCELLED);
        participationRepository.updateParticipation(participation);

        post.removeParticipant();

        if (post.getCurrentParticipants() == 0) {
            // Post를 삭제하기 전에 해당 Post의 모든 Participation을 먼저 삭제
            List<Participation> allParticipations = participationRepository.findParticipationsByPostId(post.getId());
            for (Participation p : allParticipations) {
                // 각 Participation의 ParticipationProduct 삭제
                List<ParticipationProduct> pProducts = 
                    participationProductRepository.findParticipationProductsById(p.getId());
                for (ParticipationProduct pp : pProducts) {
                    participationProductRepository.deleteParticipationProduct(pp);
                }
                // Participation 삭제
                participationRepository.deleteParticipation(p);
            }
            // 모든 Participation 삭제 후 Post 삭제
            postRepository.delete(post);
        }
    }

    //참여 시 상품 추가
    @Transactional
    public Long addProduct(Long participationId, Product product, int quantity, int totalPrice) {
        Participation participation = participationRepository.findParticipation(participationId);
        
        ParticipationProduct participationProduct = ParticipationProduct.createParticipationProduct(participation, product, quantity, totalPrice);
        participationProductRepository.saveParticipationProduct(participationProduct);
        return participationProduct.getId();
    }

    //특정 Post의 참여자 목록 조회
    public List<Participation> findParticipationsByPostId(Long postId) {
        return participationRepository.findParticipationsByPostId(postId);
    }

    //특정 Post의 참여자 목록 조회(JOINED 상태)
    public List<Participation> findParticipationsByPostIdAndStatus(Long postId, ParticipationStatus status) {
        return participationRepository.findParticipationsByPostIdAndStatus(postId, ParticipationStatus.JOINED);
    }

    //특정 참여의 상품 목록 조회
    public List<ParticipationProduct> findParticipationProductsByParticipationId(Long participationId) {
        return participationProductRepository.findParticipationProductsById(participationId);
    }
    //단건 조회
    public Participation findParticipationByParticipationId(Long participationId) {
        return participationRepository.findParticipation(participationId);
    }
    // 특정 회원의 참여 목록 조회 (상태별로 그룹화)
    @Transactional(readOnly = true)
    public Map<String, List<Participation>> findParticipationsGrouped(Long memberId) {
        List<Participation> allParticipations = 
            participationRepository.findParticipationsByMemberId(memberId);
        
        Map<String, List<Participation>> grouped = new HashMap<>();
        
        List<Participation> open = new ArrayList<>();
        List<Participation> completed = new ArrayList<>();
        List<Participation> cancelled = new ArrayList<>();
        
        for (Participation p : allParticipations) {
            PostStatus status = p.getPost().getStatus();
            p.getPost().getStore().getName();
            
            if (status == PostStatus.OPEN) {
                open.add(p);
            }
            if (status == PostStatus.ORDERED || status == PostStatus.WAITING_ORDER) {
                completed.add(p);
            }
            if (status == PostStatus.CANCELLED) {
                cancelled.add(p);
            }
        }
        
        grouped.put("open", open);
        grouped.put("completed", completed);
        grouped.put("cancelled", cancelled);
        
        return grouped;
    }

    // === 검증 메서드 ===

    private void validateDeadline(Post post) {
        if (post.getDeadline().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("마감 시간이 지난 모집글입니다.");
        }
    }

    private void validatePostStatus(Post post) {
        if (post.getStatus() != PostStatus.OPEN) {
            throw new IllegalStateException("참여할 수 없는 상태의 모집글입니다.");
        }
    }
}

