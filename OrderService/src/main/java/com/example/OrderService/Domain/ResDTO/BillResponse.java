package com.example.OrderService.Domain.ResDTO;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class BillResponse {

    private String sessionId;
    private int tableId;
    private List<BillItemResponse> items;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}
