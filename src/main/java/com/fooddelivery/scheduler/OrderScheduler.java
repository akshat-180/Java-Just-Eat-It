package com.fooddelivery.scheduler;

import com.fooddelivery.model.Order;
import com.fooddelivery.repository.OrderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderScheduler {
    private final OrderRepository orderRepository;

    public OrderScheduler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // run every 30 seconds (you can change to 60000 for 1 minute)
    @Scheduled(fixedRate = 30000)
    public void markDelivered() {
        List<Order> pending = orderRepository.findByStatus("PENDING");
        LocalDateTime now = LocalDateTime.now();

        for (Order o : pending) {
            LocalDateTime deliveryAt = o.getOrderTime().plusMinutes(o.getDeliveryTimeMinutes());
            if (!now.isBefore(deliveryAt)) { // now >= deliveryAt
                o.setStatus("DELIVERED");
                orderRepository.save(o);
                System.out.println("Order " + o.getId() + " marked as DELIVERED (scheduled).");
            }
        }
    }
}
