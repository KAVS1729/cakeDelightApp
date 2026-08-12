package com.example.orderService.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CakeDto {
    private Integer id;
    private String name;
    private BigDecimal price;
    private String imageUrl;
}