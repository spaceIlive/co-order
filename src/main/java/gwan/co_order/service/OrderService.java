package gwan.co_order.service;

import java.util.List;

import gwan.co_order.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import gwan.co_order.domain.*;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final PostRepository postRepository;
    private final ParticipationRepository participationRepository;
    private final ParticipationProductRepository participationProductRepository;
    private final OrderProductRepository orderProductRepository;
    private final GroupDeliveryRepository groupDeliveryRepository;
    
    @Transactional
    public void saveOrder(Order order) {
        orderRepository.saveOrder(order);
    }

    public Order findOrderById(Long orderId) {
        return orderRepository.findOrderById(orderId);
    }


    public List<Order> findOrdersByMemberId(Long memberId) {
        return orderRepository.findOrdersByMemberId(memberId);
    }

    @Transactional
    public void createOrdersFromExpiredPosts(LocalDateTime currentTime) {
        List<Post> waitingOrderPosts = postRepository.findWaitingOrderPosts();
        
        for (Post post : waitingOrderPosts) {
            processPostOrders(post);
            postRepository.updatePostStatus(post, PostStatus.ORDERED);
        }
    }

    private void processPostOrders(Post post) {
        GroupDelivery groupDelivery = GroupDelivery.createGroupDelivery(post);
        groupDeliveryRepository.save(groupDelivery);
        
        List<Participation> joinedParticipations = participationRepository.findParticipationsByPostIdAndStatus(
                post.getId(), ParticipationStatus.JOINED);
        
        for (Participation participation : joinedParticipations) {
            createOrderForParticipation(participation, post.getStore(), groupDelivery);
        }
    }

    private void createOrderForParticipation(Participation participation, Store store, GroupDelivery groupDelivery) {
        Delivery delivery = Delivery.createDelivery(groupDelivery, participation.getAddress());
        Order order = Order.createOrder(participation.getMember(), store, delivery);
        
        createOrderProducts(order, participation);
        
        orderRepository.saveOrder(order);
    }

    private void createOrderProducts(Order order, Participation participation) {
        List<ParticipationProduct> participationProducts = participationProductRepository.findParticipationProductsById(participation.getId());
        
        for (ParticipationProduct participationProduct : participationProducts) {
            OrderProduct orderProduct = OrderProduct.createOrderProduct(
                order, 
                participationProduct.getProduct(), 
                participationProduct.getQuantity(), 
                participationProduct.getUnitPrice());
            orderProductRepository.saveOrderProduct(orderProduct);
            order.updateTotalPrice(participationProduct.getUnitPrice(), participationProduct.getQuantity());
        }
    }
}