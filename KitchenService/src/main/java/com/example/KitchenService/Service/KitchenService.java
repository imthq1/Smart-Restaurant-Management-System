package com.example.KitchenService.Service;

import com.example.KitchenService.Domain.KitchenOrder;
import com.example.KitchenService.Domain.Res.OrderRes.OrderItemResponse;
import com.example.KitchenService.Repository.KitchenOrderRepository;
import com.example.KitchenService.Service.Kafka.KitchenProducer;
import com.example.KitchenService.Util.KitchenOrderStatus;
import jakarta.transaction.Transactional;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class KitchenService {
    private final KitchenOrderRepository kitchenOrderRepository;
    private final KitchenProducer kitchenProducer;
    private final RestTemplate restTemplate;

    public KitchenService(KitchenOrderRepository kitchenOrderRepository, KitchenProducer kitchenProducer, RestTemplate restTemplate) {
        this.kitchenOrderRepository = kitchenOrderRepository;
        this.kitchenProducer = kitchenProducer;
        this.restTemplate = restTemplate;
    }
    public List<OrderItemResponse> getItemsFromOrder(int orderId) {

        String url = "http://localhost:8080/api/v1/orders/" + orderId + "/items";

        ResponseEntity<List<OrderItemResponse>> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<OrderItemResponse>>() {}
                );

        return response.getBody();
    }
    public List<KitchenOrder> getActiveOrders() {
        return kitchenOrderRepository.findByStatusOrderByCreatedAtAsc(KitchenOrderStatus.COOKING);
    }
    public List<KitchenOrder> getActiveOrdersByStatus(KitchenOrderStatus status) {
        return kitchenOrderRepository.findByStatusOrderByCreatedAtAsc(status);
    }
    @Transactional
    public void statusOrder(Integer kitchenOrderId, KitchenOrderStatus status) {

        KitchenOrder kitchenOrder = kitchenOrderRepository.findById(kitchenOrderId)
                .orElseThrow(() -> new RuntimeException("Kitchen order not found"));

        if (status == KitchenOrderStatus.COOKING) {
            throw new IllegalArgumentException("COOKING status is auto-managed");
        }

        kitchenOrder.setStatus(status);
        kitchenOrderRepository.save(kitchenOrder);

        kitchenProducer.publishOrderStatus(kitchenOrder.getOrderId(), status);
    }

}
