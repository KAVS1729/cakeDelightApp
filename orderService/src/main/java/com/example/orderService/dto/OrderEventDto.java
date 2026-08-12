package com.example.orderService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventDto {
    private Integer id;
    private String userId;
    private String email;
    private String address;
    private LocalDateTime orderDate;
    private BigDecimal total;
    private String status;
    private List<OrderItemInfo> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemInfo {
        private String cakeName;
        private Integer quantity;
        private BigDecimal price;
    }
}
