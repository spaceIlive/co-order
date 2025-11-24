package gwan.co_order.service.dto;

import gwan.co_order.domain.GroupDelivery;
import gwan.co_order.domain.Order;
import gwan.co_order.domain.Post;

import java.util.List;

public record DeliveryGroupView(Post post, GroupDelivery groupDelivery, List<Order> orders) {
}

