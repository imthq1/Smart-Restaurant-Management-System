package com.example.OrderService.Controller;

import com.example.OrderService.Domain.Order;
import com.example.OrderService.Domain.OrderItem;
import com.example.OrderService.Domain.ReqDTO.OrderRequest;
import com.example.OrderService.Domain.ReqDTO.UpdateQuantityRequest;
import com.example.OrderService.Domain.ResDTO.*;
import com.example.OrderService.Service.OrderService;
import com.example.OrderService.Util.Enum.OrderStatus;
import com.example.OrderService.Util.Enum.TimeFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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
        Order newOrder = orderService.createOrder(tableId, sessionId, request);
        return ResponseEntity.ok(newOrder);
    }
    @GetMapping("/billing/session/{sessionId}")
    public ResponseEntity<BillResponse> getBill(@PathVariable String sessionId) {
        return ResponseEntity.ok(orderService.getBillBySession(sessionId));
    }
    @PutMapping("/items/{itemId}")
    public ResponseEntity<?> updateQuantity(
            @PathVariable Integer itemId,
            @RequestBody UpdateQuantityRequest request) {

        orderService.updateQuantity(itemId, request.getQuantity());

        return ResponseEntity.ok("Updated");
    }
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> deleteItem(@PathVariable Integer itemId) {

        orderService.deleteItem(itemId);

        return ResponseEntity.ok("Deleted");
    }
    @GetMapping("/table/{tableId}")
    public ResponseEntity<OrderTableResponse> getItemsByTable(@PathVariable int tableId) {

        return ResponseEntity.ok(
                orderService.getPendingItemsByTable(tableId)
        );
    }
    @GetMapping("/dashboard/summary")
    public ResponseEntity<OrderDashboardSummary> dashboardSummary() {
        return ResponseEntity.ok(orderService.getDashboardSummary());
    }
    @GetMapping("/{orderId}/items")
    public ResponseEntity<List<OrderItemResponse>> getOrderItems(@PathVariable int orderId) {
        return ResponseEntity.ok(orderService.getItemsByOrderId(orderId));
    }
    @GetMapping
    public ResponseEntity<PageResponse<OrderResponse>> getOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        return ResponseEntity.ok(
                orderService.getOrders(status, page, size, sortBy, direction)
        );
    }
    @GetMapping("/filter-time")
    public ResponseEntity<PageResponse<OrderResponse>> filterByTime(
            @RequestParam TimeFilter filter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                orderService.getOrdersByTime(filter, from, to, page, size)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.getOrderDetail(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Integer id,
            @RequestParam OrderStatus status
    ) {
        orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok().build();
    }
}