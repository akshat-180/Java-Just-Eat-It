package com.fooddelivery.repository;

import com.fooddelivery.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    @Query("select distinct r from Restaurant r left join fetch r.menu")
    List<Restaurant> findAllWithMenu();

    @Query("select r from Restaurant r left join fetch r.menu where r.id = :id")
    Optional<Restaurant> findByIdWithMenu(@Param("id") Long id);
}
