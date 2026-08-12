package com.example.orderService.service;
import com.example.orderService.config.RabbitMQConfig;
import com.example.orderService.dto.CakeDto;
import com.example.orderService.dto.OrderEventDto;
import com.example.orderService.model.BasketItem;
import com.example.orderService.model.CustomerOrder;
import com.example.orderService.model.OrderItem;
import com.example.orderService.repository.CustomerOrderRepo;
import com.example.orderService.repository.OrderItemRepo;
import com.example.orderService.repository.OrderServiceRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class OrderService {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private OrderServiceRepo orderServiceRepo;
    @Autowired
    private CustomerOrderRepo customerOrderRepo;
    @Autowired
    private OrderItemRepo orderItemRepo;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Value("${cake.catalog.url}")
    private String cakeCatalogUrl;
    //private static final String CAKE_CATALOG_URL = "http://localhost:8080/cakecatalog/getcake/";
    //add to basketItem
    public BasketItem addToBasket(String userId,Integer cakeId,int quantity){
        CakeDto cakedto=restTemplate.getForObject(cakeCatalogUrl+cakeId, CakeDto.class);
        if(cakedto==null){
            throw  new RuntimeException("NotFound");
        }
        BasketItem basket=orderServiceRepo.findByUserIdAndCakeId(userId,cakeId).orElse(null);
        if(basket!=null){
            int newQuantity=basket.getQuantity()+quantity;
            basket.setQuantity(newQuantity);
            return orderServiceRepo.save(basket);
        }
        BasketItem item = new BasketItem();
        item.setUserId(userId);
        item.setCakeId(cakedto.getId());
        item.setCakeName(cakedto.getName());
        item.setPrice(cakedto.getPrice());
        item.setImageUrl(cakedto.getImageUrl());
        item.setQuantity(quantity);
        return orderServiceRepo.save(item);
    }
    //To update the basketItem
    public BasketItem updateQuantity(Integer id,int newQuantity){
        BasketItem  itemFound=orderServiceRepo.findById(id).orElse(null);
        if(itemFound==null){
            throw new RuntimeException("NotFound");
        }
        itemFound.setQuantity(newQuantity);
        return orderServiceRepo.save(itemFound);
    }
    //delelte from basketItem
    public void deleteBasketItem(Integer id){
        BasketItem  itemFound=orderServiceRepo.findById(id).orElse(null);
        if(itemFound==null){
            throw new RuntimeException("NotFound");
        }
        orderServiceRepo.deleteById(id);
    }
    // get basket item of user
    public List<BasketItem> getAllBasketItem(String userId){
        return orderServiceRepo.findByUserId(userId);
    }
    // calculate total
    public BigDecimal getBasketTotal(String userId) {
        List<BasketItem> items = getAllBasketItem(userId);
        BigDecimal total = BigDecimal.ZERO;

        for (BasketItem item : items) {
            BigDecimal subtotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(subtotal);
        }
        return total;
    }

    public CustomerOrder checkout(String userId,String email,String address) {
        List<BasketItem> basketItems = getAllBasketItem(userId);

        if (basketItems.isEmpty()) {
            log.warn("Checkout attempted with empty basket for userId={}", userId);
            throw new RuntimeException("Basket is empty, cannot checkout");
        }

        BigDecimal total = getBasketTotal(userId);

        CustomerOrder customer = new CustomerOrder();
        customer.setUserId(userId);
        customer.setEmail(email);
        customer.setAddress(address);
        customer.setOrderDate(LocalDateTime.now());
        customer.setTotal(total);
        customer.setStatus("PLACED");
        CustomerOrder savedOrder = customerOrderRepo.save(customer);
        log.info("Order created: orderId={}, userId={}, total={}", savedOrder.getId(), userId, total);

        for (BasketItem item : basketItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(savedOrder.getId());
            orderItem.setCakeId(item.getCakeId());
            orderItem.setCakeName(item.getCakeName());
            orderItem.setPrice(item.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItemRepo.save(orderItem);
        }
        orderServiceRepo.deleteAll(basketItems);
        log.info("Basket cleared for userId={} after checkout", userId);
        List<OrderEventDto.OrderItemInfo> itemInfos = basketItems.stream()
                .map(item -> new OrderEventDto.OrderItemInfo(item.getCakeName(), item.getQuantity(), item.getPrice()))
                .toList();

        OrderEventDto event = new OrderEventDto(
                savedOrder.getId(), savedOrder.getUserId(), savedOrder.getEmail(), savedOrder.getAddress(),
                savedOrder.getOrderDate(), savedOrder.getTotal(), savedOrder.getStatus(), itemInfos
        );

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, event);
        log.info("Order completion event published to RabbitMQ: orderId={}, routingKey={}", savedOrder.getId(), RabbitMQConfig.ROUTING_KEY);
        return savedOrder;
      //  rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, savedOrder);
      //  return savedOrder;
    }
    //To view Specific order details
    public CustomerOrder orderDetails(Integer orderId) {
        return customerOrderRepo.findById(orderId).orElse(null);
    }
    //To update Order status
    public CustomerOrder orderStatusById(Integer orderId, String status) {
        CustomerOrder cOrder=customerOrderRepo.findById(orderId).orElse(null);
        if(cOrder==null){
            throw new RuntimeException("Order_NotFound");
        }
        cOrder.setStatus(status);
        return customerOrderRepo.save(cOrder);
    }

}
