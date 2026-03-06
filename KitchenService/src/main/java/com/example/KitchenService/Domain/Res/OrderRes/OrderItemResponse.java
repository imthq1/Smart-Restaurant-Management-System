package com.example.KitchenService.Domain.Res.OrderRes;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemResponse {

    private int id;
    private String status;
    private int productId;
    private String productName;
    private BigDecimal price;
    private int quantity;
    private String note;
    private BigDecimal subTotal;

}