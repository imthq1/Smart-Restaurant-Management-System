package com.example.OrderService.Util.Enum;

public enum OrderStatus {
    PENDING,    // Mới tạo, chờ xác nhận
    CONFIRMED,  // Bếp đã nhận đơn
    PREPARING,  // Đang chế biến
    SERVING,    // Đang phục vụ (lên món dần)
    COMPLETED,  // Đã ăn xong/Hoàn thành
    CANCELLED   // Đã hủy
}
