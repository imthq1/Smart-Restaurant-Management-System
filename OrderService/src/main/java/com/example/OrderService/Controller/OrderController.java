package com.example.OrderService.Controller;

import com.example.OrderService.Domain.Order;
import com.example.OrderService.Domain.ReqDTO.OrderRequest;
import com.example.OrderService.Service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    @PostMapping
    public ResponseEntity<Order> createOrder(
            @RequestHeader("X-Table-Id") int tableId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @RequestBody OrderRequest request
    ) {
        // Table ID này cực kỳ an toàn vì user không thể giả mạo (do đã verify token ở Gateway)
        Order newOrder = orderService.createOrder(tableId, sessionId, request);
        return ResponseEntity.ok(newOrder);
    }
}