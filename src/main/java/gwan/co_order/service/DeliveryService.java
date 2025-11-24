package gwan.co_order.service;

import gwan.co_order.domain.Delivery;
import gwan.co_order.domain.DeliveryStatus;
import gwan.co_order.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;

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
}
