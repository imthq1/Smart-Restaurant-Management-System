package com.example.OrderService.Domain;

import com.example.OrderService.Util.Enum.OrderItemStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderItemStatus status = OrderItemStatus.ORDERED;

    // Quan hệ Many-to-One: Nhiều món ăn thuộc về 1 đơn hàng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;

    @NotNull(message = "Product ID cannot be null")
    private int productId; // ID tham chiếu sang Menu Service

    @Column(nullable = false)
    private String productName; // Snapshot: Lưu cứng tên món lúc order

    @Column(nullable = false)
    @Min(value = 0, message = "Price must be positive")
    private BigDecimal price; // Snapshot: Lưu cứng giá lúc order

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private String note; // Ghi chú: "Không hành", "Ít đá"

    // Phương thức tính tổng tiền item này
    public BigDecimal getSubTotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}