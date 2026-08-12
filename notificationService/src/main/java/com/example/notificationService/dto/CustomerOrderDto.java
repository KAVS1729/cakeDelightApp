package com.example.notificationService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOrderDto {
    private Integer id;
    private String userId;
    private String email;
    private String address;
    private LocalDateTime orderDate;
    private BigDecimal total;
    private String status;
    private List<OrderItemDto> items;
}
