package com.fooddelivery.repository;

import com.fooddelivery.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(String status);
    List<Order> findByUserId(Long userId);
    List<Order> findByRestaurantId(Long restaurantId);
    List<Order> findByUserIdAndDelivered(Long userId, boolean delivered);
    List<Order> findByRestaurantIdAndDelivered(Long restaurantId, boolean delivered);
    List<Order> findByUserIdAndStatus(Long userId, String status);
    List<Order> findByRestaurantIdAndStatus(Long restaurantId, String status);

}