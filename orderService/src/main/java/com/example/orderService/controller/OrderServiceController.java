package com.example.orderService.controller;

import com.example.orderService.dto.BasketSummaryDto;
import com.example.orderService.dto.CheckoutRequest;
import com.example.orderService.model.BasketItem;
import com.example.orderService.model.CustomerOrder;
import com.example.orderService.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/order/basket")
public class OrderServiceController {

    @Autowired
    private OrderService orderService;

    //To add items to basket
    @PostMapping("/addBasket")
    public BasketItem addItemToBasket(@RequestParam String userId,@RequestParam Integer cakeId,@RequestParam int quantity){
          return orderService.addToBasket(userId,cakeId,quantity);
    }
    //To update quantity field in basket
    @PatchMapping("/updateBasket")
    public void updateBasket(@RequestParam Integer id, @RequestParam int newQuantity){
        orderService.updateQuantity(id,newQuantity);
    }
    //To delete basket by id
    @DeleteMapping("/deleteBasket/{id}")
    public void deleteBasket(@PathVariable Integer id){
        orderService.deleteBasketItem(id);
    }
   /* @GetMapping("/basketByUserId/{userId}")
    public List<BasketItem> getBasketItemByUserId(@PathVariable String userId){
        return orderService.getAllBasketItem(userId);
    }*/
    //To get summary basket of individual user
    @GetMapping("/summary/{userId}")
    public BasketSummaryDto getBasketSummary(@PathVariable String userId) {
        List<BasketItem> items = orderService.getAllBasketItem(userId);
        BigDecimal total = orderService.getBasketTotal(userId);
        return new BasketSummaryDto(items, total);
    }
    //checkout
    @PostMapping("/checkout/{userId}")
    public CustomerOrder checkout(@PathVariable String userId,@Valid @RequestBody CheckoutRequest request) {
        return orderService.checkout(userId,request.getEmail(),request.getAddress());
    }
    //View a specific order
    @GetMapping("/{orderId}")
    public CustomerOrder orderDetails(@PathVariable Integer orderId){
        return orderService.orderDetails(orderId);
    }
    //To update status and return to user
    @PatchMapping("/{orderId}/status")
    public CustomerOrder orderStatusById(@PathVariable Integer orderId,@RequestParam String status){
        return orderService.orderStatusById(orderId,status);
    }

}