package gwan.co_order.service;

import gwan.co_order.domain.Delivery;
import gwan.co_order.domain.DeliveryStatus;
import gwan.co_order.domain.GroupDelivery;
import gwan.co_order.domain.PostStatus;
import gwan.co_order.repository.DeliveryRepository;
import gwan.co_order.repository.GroupDeliveryRepository;
import gwan.co_order.repository.OrderRepository;
import gwan.co_order.service.dto.DeliveryGroupView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final GroupDeliveryRepository groupDeliveryRepository;
    private final OrderRepository orderRepository;

    public void saveDelivery(Delivery delivery) {
        deliveryRepository.saveDelivery(delivery);
    }

    public Delivery findDeliveryById(Long deliveryId) {
        return deliveryRepository.findDeliveryById(deliveryId);
    }

    public void updateDeliveryStatus(Long deliveryId, DeliveryStatus status) {
        deliveryRepository.updateDeliveryStatus(deliveryId, status);
    }

    public void deleteDelivery(Delivery delivery) {
        deliveryRepository.deleteDelivery(delivery);
    }

    public List<DeliveryGroupView> getOrderedDeliveryGroups() {
        return groupDeliveryRepository.findGroupDeliveriesByPostStatus(PostStatus.ORDERED)
                .stream()
                .map(groupDelivery -> new DeliveryGroupView(
                        groupDelivery.getPost(),
                        groupDelivery,
                        orderRepository.findOrdersByGroupDeliveryId(groupDelivery.getId())
                ))
                .toList();
    }

    @Transactional
    public void assignDriverToGroup(Long postId, String driverName, String driverContact) {
        GroupDelivery groupDelivery = groupDeliveryRepository.findGroupDeliveryByPostId(postId);
        if (groupDelivery == null) {
            throw new IllegalArgumentException("해당 모집글의 그룹 배달 정보를 찾을 수 없습니다.");
        }

        groupDelivery.assignDriver(driverName, driverContact);
        deliveryRepository.findDeliveriesByGroupDeliveryId(groupDelivery.getId())
                .forEach(delivery -> {
                    delivery.setStatus(DeliveryStatus.DELIVERING);
                });
    }

    @Transactional
    public void completeGroupDelivery(Long postId) {
        GroupDelivery groupDelivery = groupDeliveryRepository.findGroupDeliveryByPostId(postId);
        if (groupDelivery == null) {
            throw new IllegalArgumentException("해당 모집글의 그룹 배달 정보를 찾을 수 없습니다.");
        }

        groupDelivery.markCompleted();
        deliveryRepository.findDeliveriesByGroupDeliveryId(groupDelivery.getId())
                .forEach(delivery -> delivery.setStatus(DeliveryStatus.COMPLETED));
    }
}
