package gwan.co_order.service;

import gwan.co_order.domain.*;
import gwan.co_order.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ParticipationServiceTest {

    @Autowired ParticipationService participationService;
    @Autowired ParticipationRepository participationRepository;
    @Autowired PostRepository postRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired StoreRepository storeRepository;
    @Autowired ProductRepository productRepository;
    @Autowired ParticipationProductRepository participationProductRepository;
    @Autowired EntityManager em;

    private Member member;
    private Store store;
    private Post post;
    private Address address;

    @BeforeEach
    void setUp() {
        // 회원 생성
        member = new Member();
        member.setName("테스트회원");
        member.setPassword("123");
        address = new Address("서울시 강남구", 1.0, 1.0);
        member.setAddress(address);
        memberRepository.saveMember(member);

        // 가게 생성
        store = new Store();
        store.setName("테스트가게");
        store.setAddress(new Address("서울시 강남구", 1.005, 1.003));
        storeRepository.saveStore(store);

        // 모집글 생성
        post = Post.createPost(member, store, address, 2, LocalDateTime.now().plusHours(1));
        postRepository.savePost(post);
    }

    @Test
    @DisplayName("모집글 참여")
    void joinPost() {
        //given
        Member newMember = new Member();
        newMember.setName("참여자");
        newMember.setPassword("123");
        newMember.setAddress(new Address("서울시 강남구", 1.0, 1.0));
        memberRepository.saveMember(newMember);
        Long memberId = newMember.getId();

        //when
        Long participationId = participationService.joinPost(memberId, post.getId(), newMember.getAddress());

        //then
        Participation participation = participationRepository.findParticipation(participationId);
        assertNotNull(participation);
        assertEquals(ParticipationStatus.JOINED, participation.getStatus());
        assertEquals(1, post.getCurrentParticipants()); // 호스트 1명 + 참여자 1명 = 2명이지만, 호스트는 이미 참여되어 있음
    }

    @Test
    @DisplayName("중복 참여 예외")
    void duplicateParticipation() {
        //given
        Member newMember = new Member();
        newMember.setName("참여자");
        newMember.setPassword("123");
        newMember.setAddress(new Address("서울시 강남구", 1.0, 1.0));
        memberRepository.saveMember(newMember);
        Long memberId = newMember.getId();

        participationService.joinPost(memberId, post.getId(), newMember.getAddress());

        //when & then
        assertThrows(IllegalStateException.class, () -> {
            participationService.joinPost(memberId, post.getId(), newMember.getAddress());
        });
    }

    @Test
    @DisplayName("마감 시간 지난 모집글 참여 예외")
    void joinExpiredPost() {
        //given
        Post expiredPost = Post.createPost(member, store, address, 2, LocalDateTime.now().minusHours(1));
        postRepository.savePost(expiredPost);

        Member newMember = new Member();
        newMember.setName("참여자");
        newMember.setPassword("123");
        newMember.setAddress(new Address("서울시 강남구", 1.0, 1.0));
        memberRepository.saveMember(newMember);
        Long memberId = newMember.getId();

        //when & then
        assertThrows(IllegalStateException.class, () -> {
            participationService.joinPost(memberId, expiredPost.getId(), newMember.getAddress());
        });
    }

    @Test
    @DisplayName("참여 취소")
    void cancelParticipation() {
        //given
        // 호스트도 참여 (참여 인원이 0명이 되지 않도록)
        Long hostParticipationId = participationService.joinPost(member.getId(), post.getId(), address);
        
        Member newMember = new Member();
        newMember.setName("참여자");
        newMember.setPassword("123");
        newMember.setAddress(new Address("서울시 강남구", 1.0, 1.0));
        memberRepository.saveMember(newMember);
        Long memberId = newMember.getId();

        Long participationId = participationService.joinPost(memberId, post.getId(), newMember.getAddress());
        em.flush();
        int beforeParticipants = post.getCurrentParticipants();

        //when
        participationService.cancelParticipation(participationId);
        em.flush();
        em.clear();

        //then
        Participation cancelled = participationRepository.findParticipation(participationId);
        assertNotNull(cancelled);
        assertEquals(ParticipationStatus.CANCELLED, cancelled.getStatus());
        Post updatedPost = postRepository.findPostById(post.getId());
        assertNotNull(updatedPost);
        assertEquals(beforeParticipants - 1, updatedPost.getCurrentParticipants());
    }

    @Test
    @DisplayName("참여 취소 시 상품도 함께 삭제")
    void cancelParticipationWithProducts() {
        //given
        Member newMember = new Member();
        newMember.setName("참여자");
        newMember.setPassword("123");
        newMember.setAddress(new Address("서울시 강남구", 1.0, 1.0));
        memberRepository.saveMember(newMember);
        Long memberId = newMember.getId();

        Long participationId = participationService.joinPost(memberId, post.getId(), newMember.getAddress());

        // 상품 생성 및 추가
        Product product = new Product();
        product.setName("테스트상품");
        product.setPrice(10000);
        product.setStore(store);
        productRepository.saveProduct(product);

        participationService.addProduct(participationId, product, 2, 20000);

        //when
        participationService.cancelParticipation(participationId);
        em.flush();
        em.clear();

        //then
        List<ParticipationProduct> products = participationProductRepository.findParticipationProductsById(participationId);
        assertTrue(products.isEmpty());
    }

    @Test
    @DisplayName("참여 인원이 0명이 되면 Post 자동 삭제")
    void deletePostWhenNoParticipants() {
        //given
        Member newMember = new Member();
        newMember.setName("참여자");
        newMember.setPassword("123");
        newMember.setAddress(new Address("서울시 강남구", 1.0, 1.0));
        memberRepository.saveMember(newMember);
        Long memberId = newMember.getId();

        // 호스트도 참여 (Post 생성 시 호스트는 자동 참여되지 않으므로 수동으로 참여)
        Long hostParticipationId = participationService.joinPost(member.getId(), post.getId(), address);
        Long participationId = participationService.joinPost(memberId, post.getId(), newMember.getAddress());
        Long postId = post.getId();
        em.flush();

        //when - 참여자 취소
        participationService.cancelParticipation(participationId);
        em.flush();
        em.clear();

        // 호스트 취소 (이제 참여 인원이 0명이 됨)
        participationService.cancelParticipation(hostParticipationId);
        em.flush();
        em.clear();

        //then
        Post deletedPost = postRepository.findPostById(postId);
        assertNull(deletedPost);
    }

    @Test
    @DisplayName("이미 취소된 참여 취소 예외")
    void cancelAlreadyCancelledParticipation() {
        //given
        Member newMember = new Member();
        newMember.setName("참여자");
        newMember.setPassword("123");
        newMember.setAddress(new Address("서울시 강남구", 1.0, 1.0));
        memberRepository.saveMember(newMember);
        Long memberId = newMember.getId();

        Long participationId = participationService.joinPost(memberId, post.getId(), newMember.getAddress());
        participationService.cancelParticipation(participationId);

        //when & then
        assertThrows(IllegalStateException.class, () -> {
            participationService.cancelParticipation(participationId);
        });
    }

    @Test
    @DisplayName("참여 시 상품 추가")
    void addProduct() {
        //given
        Member newMember = new Member();
        newMember.setName("참여자");
        newMember.setPassword("123");
        newMember.setAddress(new Address("서울시 강남구", 1.0, 1.0));
        memberRepository.saveMember(newMember);
        Long memberId = newMember.getId();

        Long participationId = participationService.joinPost(memberId, post.getId(), newMember.getAddress());

        Product product = new Product();
        product.setName("테스트상품");
        product.setPrice(10000);
        product.setStore(store);
        productRepository.saveProduct(product);

        //when
        Long productId = participationService.addProduct(participationId, product, 2, 20000);

        //then
        ParticipationProduct participationProduct = participationProductRepository.findParticipationProductById(productId);
        assertNotNull(participationProduct);
        assertEquals(2, participationProduct.getQuantity());
        assertEquals(20000, participationProduct.getUnitPrice());
    }

    @Test
    @DisplayName("존재하지 않는 모집글에 참여 예외")
    void joinPost_InvalidPostId() {
        //given
        Member newMember = new Member();
        newMember.setName("참여자");
        newMember.setPassword("123");
        newMember.setAddress(new Address("서울시 강남구", 1.0, 1.0));
        memberRepository.saveMember(newMember);
        Long memberId = newMember.getId();

        //when & then
        assertThrows(NullPointerException.class, () -> {
            participationService.joinPost(memberId, 999L, newMember.getAddress());
        });
    }

    @Test
    @DisplayName("존재하지 않는 참여 ID로 취소 예외")
    void cancelParticipation_NotFound() {
        //when & then
        assertThrows(IllegalStateException.class, () -> {
            participationService.cancelParticipation(999L);
        });
    }

    @Test
    @DisplayName("참여 취소 - 마감 시간 지난 모집글 예외")
    void cancelParticipation_ExpiredPost() {
        //given
        Member newMember = new Member();
        newMember.setName("참여자");
        newMember.setPassword("123");
        newMember.setAddress(new Address("서울시 강남구", 1.0, 1.0));
        memberRepository.saveMember(newMember);
        Long memberId = newMember.getId();

        // 마감 시간이 지나기 전에 참여
        Long participationId = participationService.joinPost(memberId, post.getId(), newMember.getAddress());
        
        // 마감 시간을 과거로 변경
        post.setDeadline(LocalDateTime.now().minusHours(1));
        em.flush();
        em.clear();

        //when & then
        assertThrows(IllegalStateException.class, () -> {
            participationService.cancelParticipation(participationId);
        });
    }
}

