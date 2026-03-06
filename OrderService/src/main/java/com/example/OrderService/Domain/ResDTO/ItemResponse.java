package com.example.OrderService.Domain.ResDTO;


import lombok.Data;
import java.math.BigDecimal;

@Data
public class ItemResponse {
    private int id;
    private String name;
    private BigDecimal price;
    private String thumbnailUrl;
    private boolean available; // Quan trọng: dùng để check món còn bán không
}