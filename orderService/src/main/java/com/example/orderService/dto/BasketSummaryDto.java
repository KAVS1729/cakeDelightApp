package com.example.orderService.dto;

import com.example.orderService.model.BasketItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class BasketSummaryDto {
    private List<BasketItem> items;
    private BigDecimal total;
}
