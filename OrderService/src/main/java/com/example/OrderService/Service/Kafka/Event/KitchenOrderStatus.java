package com.example.OrderService.Service.Kafka.Event;

public enum KitchenOrderStatus {
    COOKING,    // Có ít nhất 1 món đang nấu
    DONE,       // Tất cả món DONE
    CANCELLED   // Tất cả món CANCELLED
}
