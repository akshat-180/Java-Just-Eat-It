package com.fooddelivery.service;

import com.fooddelivery.model.Order;
import com.fooddelivery.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order placeOrder(Order o) {
        return orderRepository.save(o);
    }

    public List<Order> listOrdersForUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> listOrdersForUser(Long userId, boolean delivered) {
        return orderRepository.findByUserIdAndDelivered(userId, delivered);
    }

    public List<Order> listOrdersForRestaurant(Long restaurantId) {
        return orderRepository.findByRestaurantId(restaurantId);
    }

    public List<Order> listOrdersForRestaurant(Long restaurantId, boolean delivered) {
        return orderRepository.findByRestaurantIdAndDelivered(restaurantId, delivered);
    }

    // optional helper to mark delivered
    public void markDelivered(Long orderId) {
        orderRepository.findById(orderId).ifPresent(o -> { o.setDelivered(true); orderRepository.save(o); });
    }
}
