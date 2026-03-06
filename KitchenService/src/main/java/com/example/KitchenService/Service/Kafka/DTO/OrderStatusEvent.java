package com.example.KitchenService.Service.Kafka.DTO;

import com.example.KitchenService.Util.KitchenOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusEvent {
    private Integer orderId;
    private KitchenOrderStatus status;
}
