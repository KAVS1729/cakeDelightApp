package com.example.orderService.repository;

import com.example.orderService.model.BasketItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface OrderServiceRepo extends JpaRepository<BasketItem,Integer> {
    List<BasketItem> findByUserId(String userId);
    Optional<BasketItem> findByUserIdAndCakeId(String userId, Integer cakeId);
}
