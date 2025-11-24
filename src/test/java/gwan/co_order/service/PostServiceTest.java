package gwan.co_order.service;

import gwan.co_order.domain.*;
import gwan.co_order.repository.MemberRepository;
import gwan.co_order.repository.PostRepository;
import gwan.co_order.repository.StoreRepository;
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
class PostServiceTest {

    @Autowired PostService postService;
    @Autowired PostRepository postRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired StoreRepository storeRepository;
    @Autowired EntityManager em;

    private Member member;
    private Store store;
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
    }

    @Test
    @DisplayName("모집글 생성")
    void createPost() {
        //given
        LocalDateTime deadline = LocalDateTime.now().plusHours(1);

        //when
        Post post = postService.createPost(member.getId(), store.getId(), address, deadline);
        em.flush();
        em.clear();

        //then
        Post foundPost = postRepository.findPostById(post.getId());
        assertNotNull(foundPost);
        assertEquals(member.getId(), foundPost.getHost().getId());
        assertEquals(store.getId(), foundPost.getStore().getId());
        assertEquals(PostStatus.OPEN, foundPost.getStatus());
        assertEquals(0, foundPost.getCurrentParticipants());
        assertTrue(foundPost.getMinParticipants() > 0); // 정책에 의해 계산됨
    }

    @Test
    @DisplayName("모집글 조회")
    void findPostById() {
        //given
        LocalDateTime deadline = LocalDateTime.now().plusHours(1);
        Post post = postService.createPost(member.getId(), store.getId(), address, deadline);
        Long postId = post.getId();
        em.flush();
        em.clear();

        //when
        Post foundPost = postService.findPostById(postId);

        //then
        assertNotNull(foundPost);
        assertEquals(postId, foundPost.getId());
        assertEquals(PostStatus.OPEN, foundPost.getStatus());
    }

    @Test
    @DisplayName("주변 모집글 조회")
    void findNearOpenPosts() {
        //given
        LocalDateTime deadline = LocalDateTime.now().plusHours(1);
        
        // 1km 이내 가게
        Store nearbyStore = new Store();
        nearbyStore.setName("가까운가게");
        nearbyStore.setAddress(new Address("서울시 강남구", 1.005, 1.003));
        storeRepository.saveStore(nearbyStore);
        
        Post nearbyPost = postService.createPost(member.getId(), nearbyStore.getId(), address, deadline);
        nearbyPost.setStatus(PostStatus.OPEN);
        em.flush();
        em.clear();

        //when
        List<Post> posts = postService.findNearOpenPosts(1.0, 1.0);

        //then
        assertFalse(posts.isEmpty());
        assertTrue(posts.stream().anyMatch(p -> p.getId().equals(nearbyPost.getId())));
    }

    @Test
    @DisplayName("만료된 모집글 처리 - 최소 인원 미달")
    void processExpiredOpenPosts_NotEnoughParticipants() {
        //given
        LocalDateTime pastDeadline = LocalDateTime.now().minusHours(1);
        Post post = postService.createPost(member.getId(), store.getId(), address, pastDeadline);
        post.setStatus(PostStatus.OPEN);
        post.setCurrentParticipants(1);
        post.setMinParticipants(3); // 최소 인원보다 적음
        em.flush();
        em.clear();

        //when
        postService.processExpiredOpenPosts(LocalDateTime.now());
        em.flush();
        em.clear();

        //then
        Post updatedPost = postRepository.findPostById(post.getId());
        assertEquals(PostStatus.CANCELLED, updatedPost.getStatus());
    }

    @Test
    @DisplayName("만료된 모집글 처리 - 최소 인원 충족")
    void processExpiredOpenPosts_EnoughParticipants() {
        //given
        LocalDateTime pastDeadline = LocalDateTime.now().minusHours(1);
        Post post = postService.createPost(member.getId(), store.getId(), address, pastDeadline);
        post.setStatus(PostStatus.OPEN);
        post.setCurrentParticipants(5);
        post.setMinParticipants(3); // 최소 인원 충족
        em.flush();
        em.clear();

        //when
        postService.processExpiredOpenPosts(LocalDateTime.now());
        em.flush();
        em.clear();

        //then
        Post updatedPost = postRepository.findPostById(post.getId());
        assertEquals(PostStatus.WAITING_ORDER, updatedPost.getStatus());
    }

    @Test
    @DisplayName("모집글 삭제")
    void deletePost() {
        //given
        LocalDateTime deadline = LocalDateTime.now().plusHours(1);
        Post post = postService.createPost(member.getId(), store.getId(), address, deadline);
        Long postId = post.getId();
        em.flush();
        em.clear();

        //when
        postService.deletePost(postId);
        em.flush();
        em.clear();

        //then
        Post deletedPost = postRepository.findPostById(postId);
        assertNull(deletedPost);
    }

    @Test
    @DisplayName("존재하지 않는 모집글 ID로 조회")
    void findPostById_NotFound() {
        //when
        Post foundPost = postService.findPostById(999L);

        //then
        assertNull(foundPost);
    }


}

