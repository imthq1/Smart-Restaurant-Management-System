package com.example.OrderService.Domain.ReqDTO;


import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private List<OrderItemRequest> items;
}