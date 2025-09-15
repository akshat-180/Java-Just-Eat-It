
package com.fooddelivery.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Builder.Default
    private boolean delivered = false;
    @ManyToOne
    private User user;
    @Column(nullable = false)
    private String status;
    @ManyToOne
    private Restaurant restaurant;

    private String foodName;
    private double foodPrice;


    private int deliveryTimeMinutes;

    private LocalDateTime orderTime;


    public void setDeliveryTimeMinutes(int minutes) {
        this.deliveryTimeMinutes = minutes;
    }
}
