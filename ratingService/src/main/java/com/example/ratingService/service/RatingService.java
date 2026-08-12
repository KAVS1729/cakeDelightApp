package com.example.ratingService.service;

import com.example.ratingService.model.Rating;
import com.example.ratingService.repository.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class RatingService {
    @Autowired
    private RatingRepository ratingRepository;
    //To submit Rating
    public Rating submitRating(Rating rating) {
         Rating user=ratingRepository.findByUserIdAndCakeId(rating.getUserId(),rating.getCakeId());
         if(user!=null){
             user.setRating(rating.getRating());
             return ratingRepository.save(user);
         }
         return ratingRepository.save(rating);
    }
    //To get Rating by cakeId
    public List<Rating> getRatingByCakeId(Integer cakeId) {
          return ratingRepository.findAllByCakeId(cakeId);
    }
    //To calculate average of rating
    public double averageRatingPerCake(Integer cakeId) {
        List<Rating> ratingList = ratingRepository.findAllByCakeId(cakeId);
        if (ratingList.isEmpty()) {
            return 0.0;
        }
        int sum = 0;
        for (Rating rate : ratingList) {
            sum = sum + rate.getRating();
        }
        return (double) sum / ratingList.size();
    }

}
