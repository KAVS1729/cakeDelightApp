package com.example.ratingService.repository;

import com.example.ratingService.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface RatingRepository extends JpaRepository<Rating,Integer> {
     Rating findByUserIdAndCakeId(String userId,Integer cakeId);
     List<Rating> findAllByCakeId(Integer cakeId);
}
