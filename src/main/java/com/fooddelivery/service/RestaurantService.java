package com.fooddelivery.service;

import com.fooddelivery.model.FoodItem;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.repository.FoodItemRepository;
import com.fooddelivery.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final FoodItemRepository foodItemRepository;

    public RestaurantService(RestaurantRepository restaurantRepository,
                             FoodItemRepository foodItemRepository) {
        this.restaurantRepository = restaurantRepository;
        this.foodItemRepository = foodItemRepository;
    }

    @Transactional
    public Restaurant addRestaurant(Restaurant r) {
        return restaurantRepository.save(r);
    }

    @Transactional
    public FoodItem addFoodItem(Long restaurantId, FoodItem food) {
        Restaurant r = restaurantRepository.findById(restaurantId).orElseThrow();
        food.setRestaurant(r);
        FoodItem saved = foodItemRepository.save(food);
        r.getMenu().add(saved);
        restaurantRepository.save(r);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Restaurant> listAll() {
        return restaurantRepository.findAllWithMenu();
    }

    @Transactional(readOnly = true)
    public Optional<Restaurant> findById(Long id) {
        return restaurantRepository.findByIdWithMenu(id);
    }
    @Transactional(readOnly = true)
    public Optional<Restaurant> findByIdAndOwner(Long id, String ownerUsername) {
        return restaurantRepository.findById(id)
                .filter(r -> r.getOwnerUsername().equals(ownerUsername));
    }
}
