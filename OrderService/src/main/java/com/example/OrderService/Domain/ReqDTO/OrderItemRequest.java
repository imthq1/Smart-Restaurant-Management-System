package com.example.OrderService.Domain.ReqDTO;

import lombok.Data;

@Data
public class OrderItemRequest {
    private int productId;
    private Integer quantity;
    private String note;
}