package com.example.OrderService.Service.Kafka;

import com.example.OrderService.Domain.Kafka.OrderCreatedEvent;
import com.example.OrderService.Domain.Kafka.OrderItemEvent;
import com.example.OrderService.Domain.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "order-created";

    public void publishOrderCreated(Order order) {

        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(order.getId())
                .tableId(order.getTableId())
                .sessionId(order.getSessionId())
                .items(order.getOrderItems().stream()
                        .map(item -> OrderItemEvent.builder()
                                .orderItemId(item.getId())
                                .productId(item.getProductId())
                                .productName(item.getProductName())
                                .quantity(item.getQuantity())
                                .note(item.getNote())
                                .build())
                        .toList())
                .build();

        kafkaTemplate.send(TOPIC, order.getSessionId(), event);

        log.info("Published ORDER_CREATED event for order {}", order.getId());
    }
}
