package com.example.OrderService.Service;

import com.example.OrderService.Domain.Order;
import com.example.OrderService.Domain.OrderItem;

import com.example.OrderService.Domain.ReqDTO.OrderItemRequest;
import com.example.OrderService.Domain.ReqDTO.OrderRequest;
import com.example.OrderService.Domain.ResDTO.ItemResponse;
import com.example.OrderService.Domain.ResDTO.RestResponse;
import com.example.OrderService.Repository.MenuClient;
import com.example.OrderService.Repository.OrderRepository;
import com.example.OrderService.Service.Kafka.OrderEventProducer;
import com.example.OrderService.Util.Enum.OrderItemStatus;
import com.example.OrderService.Util.Enum.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuClient menuClient;
    private final OrderEventProducer orderEventProducer;
    @Transactional
    public Order createOrder(int tableId, String sessionId, OrderRequest request) {

        // 1. Lấy danh sách ID sản phẩm từ request của khách
        List<Integer> productIds = request.getItems().stream()
                .map(OrderItemRequest::getProductId)
                .distinct() // Loại bỏ ID trùng lặp để tối ưu request
                .collect(Collectors.toList());

        if (productIds.isEmpty()) {
            throw new RuntimeException("Đơn hàng phải có ít nhất 1 món");
        }

        // 2. Gọi sang Menu Service
        RestResponse<List<ItemResponse>> responseWrapper = menuClient.getProductsByIds(productIds);

        // Check xem gọi thành công không
        if (responseWrapper == null || responseWrapper.getStatusCode() != 200 || responseWrapper.getData() == null) {
            log.error("Lỗi khi gọi Menu Service: {}", responseWrapper != null ? responseWrapper.getMessage() : "Null Response");
            throw new RuntimeException("Không thể xác thực thông tin món ăn từ hệ thống");
        }

        List<ItemResponse> productInfos = responseWrapper.getData();
        System.out.println("productInfos: " + productInfos);
        Map<Integer, ItemResponse> productMap = productInfos.stream()
                .collect(Collectors.toMap(ItemResponse::getId, Function.identity()));

        // 4. Khởi tạo Order
        Order order = Order.builder()
                .tableId(tableId)
                 .sessionId(sessionId)
                .status(OrderStatus.PENDING)
                .build();

        // 5. Duyệt qua từng món khách đặt và validate
        List<OrderItem> orderItems = request.getItems().stream().map(itemRequest -> {

            ItemResponse product = productMap.get(itemRequest.getProductId());

            // Validate 1:
            if (product == null) {
                throw new RuntimeException("Món ăn với ID " + itemRequest.getProductId() + " không tồn tại hoặc đã bị xóa");
            }

            // Validate 2:
            if (!product.isAvailable()) {
                throw new RuntimeException("Món '" + product.getName() + "' hiện đang tạm ngưng phục vụ");
            }


            return OrderItem.builder()
                    .order(order)
                    .productId(product.getId())
                    .quantity(itemRequest.getQuantity())
                    .note(itemRequest.getNote())
                    .price(product.getPrice())
                    .productName(product.getName())
                    .status(OrderItemStatus.ORDERED)
                    .build();

        }).collect(Collectors.toList());


        order.setOrderItems(orderItems);

        orderItems.forEach(item -> item.setOrder(order));

        order.recalculateTotal();
        Order savedOrder = orderRepository.save(order);

        orderEventProducer.publishOrderCreated(savedOrder);
        return savedOrder;
    }
}