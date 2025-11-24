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
class OrderServiceTest {

    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired StoreRepository storeRepository;
    @Autowired PostRepository postRepository;
    @Autowired ParticipationRepository participationRepository;
    @Autowired ParticipationProductRepository participationProductRepository;
    @Autowired ProductRepository productRepository;
    @Autowired GroupDeliveryRepository groupDeliveryRepository;
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

        // 상품 생성
        Product product = new Product();
        product.setName("테스트상품");
        product.setPrice(10000);
        product.setStore(store);
        productRepository.saveProduct(product);

        // 모집글 생성
        LocalDateTime deadline = LocalDateTime.now().plusHours(1);
        post = Post.createPost(member, store, address, 2, deadline);
        post.setStatus(PostStatus.WAITING_ORDER);
        post.setCurrentParticipants(2);
        postRepository.savePost(post);
    }

    @Test
    @DisplayName("주문 저장")
    void saveOrder() {
        //given
        GroupDelivery groupDelivery = GroupDelivery.createGroupDelivery(post);
        groupDeliveryRepository.save(groupDelivery);
        Delivery delivery = Delivery.createDelivery(groupDelivery, address);
        Order order = Order.createOrder(member, store, delivery);

        //when
        orderService.saveOrder(order);
        em.flush();
        em.clear();

        //then
        Order foundOrder = orderRepository.findOrderById(order.getId());
        assertNotNull(foundOrder);
        assertEquals(member.getId(), foundOrder.getMember().getId());
        assertEquals(store.getId(), foundOrder.getStore().getId());
    }

    @Test
    @DisplayName("주문 조회")
    void findOrderById() {
        //given
        GroupDelivery groupDelivery = GroupDelivery.createGroupDelivery(post);
        groupDeliveryRepository.save(groupDelivery);
        Delivery delivery = Delivery.createDelivery(groupDelivery, address);
        Order order = Order.createOrder(member, store, delivery);
        orderService.saveOrder(order);
        Long orderId = order.getId();
        em.flush();
        em.clear();

        //when
        Order foundOrder = orderService.findOrderById(orderId);

        //then
        assertNotNull(foundOrder);
        assertEquals(orderId, foundOrder.getId());
    }

    @Test
    @DisplayName("회원별 주문 목록 조회")
    void findOrdersByMemberId() {
        //given
        GroupDelivery groupDelivery = GroupDelivery.createGroupDelivery(post);
        groupDeliveryRepository.save(groupDelivery);
        Delivery delivery = Delivery.createDelivery(groupDelivery, address);
        Order order1 = Order.createOrder(member, store, delivery);
        orderService.saveOrder(order1);

        // 다른 회원 생성
        Member member2 = new Member();
        member2.setName("다른회원");
        member2.setPassword("123");
        member2.setAddress(new Address("서울시 서초구", 2.0, 2.0));
        memberRepository.saveMember(member2);

        Delivery delivery2 = Delivery.createDelivery(groupDelivery, member2.getAddress());
        Order order2 = Order.createOrder(member2, store, delivery2);
        orderService.saveOrder(order2);
        em.flush();
        em.clear();

        //when
        List<Order> orders = orderService.findOrdersByMemberId(member.getId());

        //then
        assertEquals(1, orders.size());
        assertEquals(order1.getId(), orders.get(0).getId());
    }

    @Test
    @DisplayName("만료된 모집글에서 주문 생성")
    void createOrdersFromExpiredPosts() {
        //given
        // 참여자 1 생성
        Participation participation1 = Participation.createParticipation(member, post, address);
        participation1.setStatus(ParticipationStatus.JOINED);
        participationRepository.saveParticipation(participation1);

        // 참여자 2 생성
        Member member2 = new Member();
        member2.setName("참여자2");
        member2.setPassword("123");
        member2.setAddress(new Address("서울시 서초구", 1.002, 1.003));
        memberRepository.saveMember(member2);
        Participation participation2 = Participation.createParticipation(member2, post, member2.getAddress());
        participation2.setStatus(ParticipationStatus.JOINED);
        participationRepository.saveParticipation(participation2);

        // 상품 생성
        Product product = productRepository.findProductsByStoreId(store.getId()).get(0);

        // 참여 상품 추가
        ParticipationProduct pp1 = new ParticipationProduct();
        pp1.setParticipation(participation1);
        pp1.setProduct(product);
        pp1.setQuantity(2);
        pp1.setUnitPrice(10000);
        participationProductRepository.saveParticipationProduct(pp1);

        ParticipationProduct pp2 = new ParticipationProduct();
        pp2.setParticipation(participation2);
        pp2.setProduct(product);
        pp2.setQuantity(1);
        pp2.setUnitPrice(10000);
        participationProductRepository.saveParticipationProduct(pp2);

        em.flush();
        em.clear();

        //when
        orderService.createOrdersFromExpiredPosts(LocalDateTime.now());
        em.flush();
        em.clear();

        //then
        Post updatedPost = postRepository.findPostById(post.getId());
        assertEquals(PostStatus.ORDERED, updatedPost.getStatus());

        List<Order> orders = orderService.findOrdersByMemberId(member.getId());
        assertEquals(1, orders.size());
        Order order = orders.get(0);
        assertEquals(20000, order.getTotalPrice()); // 10000 * 2

        List<Order> orders2 = orderService.findOrdersByMemberId(member2.getId());
        assertEquals(1, orders2.size());
        Order order2 = orders2.get(0);
        assertEquals(10000, order2.getTotalPrice()); // 10000 * 1
    }

    @Test
    @DisplayName("존재하지 않는 주문 ID로 조회")
    void findOrderById_NotFound() {
        //when
        Order foundOrder = orderService.findOrderById(999L);

        //then
        assertNull(foundOrder);
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로 주문 목록 조회")
    void findOrdersByMemberId_NotFound() {
        //when
        List<Order> orders = orderService.findOrdersByMemberId(999L);

        //then
        assertTrue(orders.isEmpty());
    }

    @Test
    @DisplayName("만료된 모집글에서 주문 생성 - 참여자가 없는 경우")
    void createOrdersFromExpiredPosts_NoParticipants() {
        //given
        // 참여자가 없는 WAITING_ORDER 상태 Post
        post.setStatus(PostStatus.WAITING_ORDER);
        post.setCurrentParticipants(0);
        em.flush();
        em.clear();

        //when
        orderService.createOrdersFromExpiredPosts(LocalDateTime.now());
        em.flush();
        em.clear();

        //then
        Post updatedPost = postRepository.findPostById(post.getId());
        assertEquals(PostStatus.ORDERED, updatedPost.getStatus());

        // 주문은 생성되지 않음
        List<Order> orders = orderService.findOrdersByMemberId(member.getId());
        assertTrue(orders.isEmpty());
    }
}

