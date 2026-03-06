package com.example.OrderService.Service;

import com.example.OrderService.Domain.Order;
import com.example.OrderService.Domain.OrderItem;

import com.example.OrderService.Domain.ReqDTO.OrderItemRequest;
import com.example.OrderService.Domain.ReqDTO.OrderRequest;
import com.example.OrderService.Domain.ResDTO.*;
import com.example.OrderService.Repository.MenuClient;
import com.example.OrderService.Repository.OrderItemRepository;
import com.example.OrderService.Repository.OrderRepository;
import com.example.OrderService.Service.Kafka.OrderEventProducer;
import com.example.OrderService.Util.Enum.OrderItemStatus;
import com.example.OrderService.Util.Enum.OrderStatus;
import com.example.OrderService.Util.Enum.TimeFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public Order createOrder(int tableId, String sessionId, OrderRequest request) {

        // 1. Lấy danh sách ID sản phẩm từ request của khách
        List<Integer> productIds = request.getItems().stream()
                .map(OrderItemRequest::getProductId)
                .distinct()
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
    public OrderTableResponse getPendingItemsByTable(int tableId) {

        List<Order> orders = orderRepository
                .findByTableIdAndStatus(tableId, OrderStatus.COMPLETED);

        // Lấy tất cả orderItems từ các orders
        List<OrderItemResponse> items = orders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .status(item.getStatus().name())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .note(item.getNote())
                        .subTotal(item.getSubTotal())
                        .build())
                .toList();

        // tính tổng tiền
        BigDecimal totalAmount = items.stream()
                .map(OrderItemResponse::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return OrderTableResponse.builder()
                .items(items)
                .totalAmount(totalAmount)
                .build();
    }
    public void updateQuantity(Integer itemId, int quantity) {

        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow();

        if(quantity <= 0){
            orderItemRepository.delete(item);
        }else{
            item.setQuantity(quantity);
            orderItemRepository.save(item);
        }
    }
    public void deleteItem(Integer itemId) {
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow();
        orderItemRepository.delete(item);
    }
    public BillResponse getBillBySession(String sessionId) {
        List<Order> orders = orderRepository
                .findBySessionIdAndStatus(sessionId, OrderStatus.PENDING);

        if (orders.isEmpty()) {
            throw new RuntimeException("No active order for this session");
        }

        int tableId = orders.get(0).getTableId();

        List<BillItemResponse> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        Map<String, BillItemResponse> billMap = new LinkedHashMap<>();

        for (Order order : orders) {
            for (OrderItem item : order.getOrderItems()) {

                if (item.getStatus() != OrderItemStatus.DONE) continue;

                String key = item.getProductName();

                BigDecimal subTotal = item.getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()));

                billMap.compute(key, (k, v) -> {
                    if (v == null) {
                        return BillItemResponse.builder()
                                .productName(item.getProductName())
                                .price(item.getPrice())
                                .quantity(item.getQuantity())
                                .subTotal(subTotal)
                                .build();
                    } else {
                        v.setQuantity(v.getQuantity() + item.getQuantity());
                        v.setSubTotal(v.getSubTotal().add(subTotal));
                        return v;
                    }
                });

                total = total.add(subTotal);
            }
        }

        items = new ArrayList<>(billMap.values());


        return BillResponse.builder()
                .sessionId(sessionId)
                .tableId(tableId)
                .items(items)
                .totalAmount(total)
                .createdAt(LocalDateTime.now())
                .build();
    }
    public OrderDashboardSummary getDashboardSummary() {
        return new OrderDashboardSummary(
                orderRepository.countTotalOrders(),
                orderRepository.countByStatus(OrderStatus.PENDING),
                orderRepository.countByStatus(OrderStatus.COMPLETED),
                orderRepository.countByStatus(OrderStatus.CANCELLED)
        );
    }

    public List<OrderItemResponse> getItemsByOrderId(int orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return order.getOrderItems()
                .stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .status(item.getStatus().name())
                        .note(item.getNote())
                        .subTotal(item.getSubTotal())
                        .build())
                .toList();
    }
    public PageResponse<OrderResponse> getOrders(
            OrderStatus status,
            int page,
            int size,
            String sortBy,
            String direction
    ) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Order> orderPage = (status == null)
                ? orderRepository.findAll(pageable)
                : orderRepository.findByStatus(status, pageable);

        List<OrderResponse> data = orderPage.getContent().stream().map(order -> {

            int totalQty = orderItemRepository
                    .findByOrderId(order.getId())
                    .stream()
                    .mapToInt(OrderItem::getQuantity)
                    .sum();

            return new OrderResponse(
                    order.getId(),
                    order.getTableId(),
                    order.getCreatedAt(),
                    "Guest",
                    "DINE_IN",
                    totalQty,
                    order.getTotalAmount(),
                    order.getStatus()
            );
        }).toList();

        return new PageResponse<>(
                data,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.isLast()
        );
    }
    public PageResponse<OrderResponse> getOrdersByTime(
            TimeFilter filter,
            LocalDate from,
            LocalDate to,
            int page,
            int size
    ) {

        LocalDateTime start;
        LocalDateTime end = LocalDateTime.now();

        LocalDate today = LocalDate.now();

        switch (filter) {
            case TODAY -> start = today.atStartOfDay();

            case THIS_WEEK -> start = today.with(DayOfWeek.MONDAY).atStartOfDay();

            case THIS_MONTH -> start = today.withDayOfMonth(1).atStartOfDay();

            case THIS_YEAR -> start = today.withDayOfYear(1).atStartOfDay();

            case CUSTOM -> {
                start = from.atStartOfDay();
                end = to.atTime(23, 59, 59);
            }

            default -> throw new IllegalArgumentException("Invalid time filter");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Order> pageResult =
                orderRepository.findByCreatedAtBetween(start, end, pageable);
        List<OrderResponse> data=pageResult.getContent().stream().map(order ->
        {
            int totalQty = orderItemRepository
                    .findByOrderId(order.getId())
                    .stream()
                    .mapToInt(OrderItem::getQuantity)
                    .sum();

            return new OrderResponse(
                    order.getId(),
                    order.getTableId(),
                    order.getCreatedAt(),
                    "Guest",
                    "DINE_IN",
                    totalQty,
                    order.getTotalAmount(),
                    order.getStatus()
            );
        }).toList();

        return new PageResponse<>(
                data,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast()
        );
    }

    public Order getOrderDetail(Integer orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public void updateOrderStatus(Integer orderId, OrderStatus status) {
        Order order = getOrderDetail(orderId);
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
    }

}