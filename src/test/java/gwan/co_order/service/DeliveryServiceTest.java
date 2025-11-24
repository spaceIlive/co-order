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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DeliveryServiceTest {

    @Autowired DeliveryService deliveryService;
    @Autowired DeliveryRepository deliveryRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired StoreRepository storeRepository;
    @Autowired PostRepository postRepository;
    @Autowired GroupDeliveryRepository groupDeliveryRepository;
    @Autowired EntityManager em;

    private Member member;
    private Store store;
    private Post post;
    private Address address;
    private GroupDelivery groupDelivery;

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
        LocalDateTime deadline = LocalDateTime.now().plusHours(1);
        post = Post.createPost(member, store, address, 2, deadline);
        post.setStatus(PostStatus.WAITING_ORDER);
        postRepository.savePost(post);

        // 그룹 배달 생성
        groupDelivery = GroupDelivery.createGroupDelivery(post);
        groupDeliveryRepository.save(groupDelivery);
    }

    @Test
    @DisplayName("배달 저장")
    void saveDelivery() {
        //given
        Delivery delivery = Delivery.createDelivery(groupDelivery, address);

        //when
        deliveryService.saveDelivery(delivery);
        em.flush();
        em.clear();

        //then
        Delivery foundDelivery = deliveryRepository.findDeliveryById(delivery.getId());
        assertNotNull(foundDelivery);
        assertEquals(DeliveryStatus.READY, foundDelivery.getStatus());
        assertEquals(address.getAddress(), foundDelivery.getAddress().getAddress());
    }

    @Test
    @DisplayName("배달 조회")
    void findDeliveryById() {
        //given
        Delivery delivery = Delivery.createDelivery(groupDelivery, address);
        deliveryService.saveDelivery(delivery);
        Long deliveryId = delivery.getId();
        em.flush();
        em.clear();

        //when
        Delivery foundDelivery = deliveryService.findDeliveryById(deliveryId);

        //then
        assertNotNull(foundDelivery);
        assertEquals(deliveryId, foundDelivery.getId());
        assertEquals(DeliveryStatus.READY, foundDelivery.getStatus());
    }

    @Test
    @DisplayName("배달 상태 업데이트")
    void updateDeliveryStatus() {
        //given
        Delivery delivery = Delivery.createDelivery(groupDelivery, address);
        deliveryService.saveDelivery(delivery);
        Long deliveryId = delivery.getId();
        em.flush();
        em.clear();

        //when
        deliveryService.updateDeliveryStatus(deliveryId, DeliveryStatus.DELIVERING);
        em.flush();
        em.clear();

        //then
        Delivery updatedDelivery = deliveryRepository.findDeliveryById(deliveryId);
        assertEquals(DeliveryStatus.DELIVERING, updatedDelivery.getStatus());
    }

    @Test
    @DisplayName("배달 삭제")
    void deleteDelivery() {
        //given
        Delivery delivery = Delivery.createDelivery(groupDelivery, address);
        deliveryService.saveDelivery(delivery);
        Long deliveryId = delivery.getId();
        em.flush();
        em.clear();

        //when
        Delivery foundDelivery = deliveryRepository.findDeliveryById(deliveryId);
        deliveryService.deleteDelivery(foundDelivery);
        em.flush();
        em.clear();

        //then
        Delivery deletedDelivery = deliveryRepository.findDeliveryById(deliveryId);
        assertNull(deletedDelivery);
    }

    @Test
    @DisplayName("존재하지 않는 배달 ID로 조회")
    void findDeliveryById_NotFound() {
        //when
        Delivery foundDelivery = deliveryService.findDeliveryById(999L);

        //then
        assertNull(foundDelivery);
    }

}

