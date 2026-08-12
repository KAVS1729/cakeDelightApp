package com.example.ratingService.controller;

import com.example.ratingService.model.Rating;
import com.example.ratingService.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/rating")
public class RatingServiceController {
    @Autowired
    private RatingService ratingService;
    @PostMapping("/submitRating")
    public Rating submitRating(@Valid @RequestBody Rating rating){
         return ratingService.submitRating(rating);
    }
    @GetMapping("/getRating/{cakeId}")
    public List<Rating> getRatingByCakeId(@PathVariable Integer cakeId){
         return ratingService.getRatingByCakeId(cakeId);
    }
    @GetMapping("/average/{cakeId}")
    public double averageRatingPerCake(@PathVariable Integer cakeId){
        return ratingService.averageRatingPerCake(cakeId);
    }
}
