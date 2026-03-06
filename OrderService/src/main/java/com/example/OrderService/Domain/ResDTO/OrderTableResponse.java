package com.example.OrderService.Domain.ResDTO;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderTableResponse {

    private List<OrderItemResponse> items;

    private BigDecimal totalAmount;

}
