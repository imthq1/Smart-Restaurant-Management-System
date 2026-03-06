package com.example.KitchenService.Service.Kafka;

import com.example.KitchenService.Service.Kafka.DTO.OrderStatusEvent;
import com.example.KitchenService.Util.KitchenOrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KitchenProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "done-order";
    public void publishOrderStatus(Integer orderId, KitchenOrderStatus status) {
        OrderStatusEvent event = new OrderStatusEvent(orderId, status);
        kafkaTemplate.send(TOPIC, event);
    }

}
