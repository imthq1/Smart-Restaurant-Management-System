package com.example.KitchenService.Controller;

import com.example.KitchenService.Domain.KitchenOrder;
import com.example.KitchenService.Domain.Res.OrderRes.OrderItemResponse;
import com.example.KitchenService.Service.KitchenService;
import com.example.KitchenService.Util.KitchenOrderStatus;
import jakarta.persistence.Table;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/kitchens")
public class KitchenController {
    private final KitchenService kitchenService;
    public KitchenController(KitchenService kitchenService) {
        this.kitchenService = kitchenService;
    }
    @GetMapping("/orders")
    public ResponseEntity<List<KitchenOrder>> getOrders(
            @RequestParam(required = false) KitchenOrderStatus status
    ) {
        if (status == null) {
            return ResponseEntity.ok(kitchenService.getActiveOrders());
        }
        return ResponseEntity.ok(kitchenService.getActiveOrdersByStatus(status));
    }
    @GetMapping("/kitchen/{orderId}/items")
    public List<OrderItemResponse> getKitchenItems(@PathVariable int orderId) {
        return kitchenService.getItemsFromOrder(orderId);
    }
    @PutMapping("/setStatusOrder/{orderId}")
    public void setStatusOrder(
            @PathVariable Integer orderId,
            @RequestBody KitchenOrderStatus status
    ) {
        kitchenService.statusOrder(orderId, status);
    }

}
