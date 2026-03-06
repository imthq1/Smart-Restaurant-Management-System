package com.example.OrderService.Domain.ResDTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Builder
@Setter
public class BillItemResponse {

    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subTotal;
}