package com.example.OrderService.Service.Kafka;

import com.example.OrderService.Domain.Order;
import com.example.OrderService.Repository.OrderRepository;
import com.example.OrderService.Service.Kafka.Event.KitchenOrderStatus;
import com.example.OrderService.Service.Kafka.Event.OrderStatusEvent;
import com.example.OrderService.Util.Enum.OrderItemStatus;
import com.example.OrderService.Util.Enum.OrderStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "done-order", groupId = "order-service")
    @Transactional
    public void listen(OrderStatusEvent event) {

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (event.getStatus() == KitchenOrderStatus.DONE) {
            order.getOrderItems().forEach(i -> i.setStatus(OrderItemStatus.DONE));
            order.setStatus(OrderStatus.COMPLETED);
        }

        if (event.getStatus() == KitchenOrderStatus.CANCELLED) {
            order.getOrderItems().forEach(i -> i.setStatus(OrderItemStatus.CANCELLED));
            order.setStatus(OrderStatus.CANCELLED);
        }

        orderRepository.save(order);
    }


}
