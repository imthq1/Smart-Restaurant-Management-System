import React, { useEffect, useState, useRef } from "react";
import { FaSearch, FaSyncAlt, FaClock } from "react-icons/fa";
import {
  getKitchenOrders,
  getOrderItems, // Import hàm mới
  updateOrderStatus,
} from "../../../services/kitcheen/kitchenService";
import {
  connectToKitchenSocket,
  disconnectSocket,
} from "../../../services/websocket/socketService";
import "../../../styles/kitchen-order.css";

import notificationSound from "../../../assets/bell-notification-337658.mp3";

const KitchenOrder = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [updatingOrderId, setUpdatingOrderId] = useState(null);

  const audioRef = useRef(new Audio(notificationSound));

  const fetchOrders = async () => {
    setLoading(true);
    try {
      // Bước 1: Lấy danh sách thông tin chung của đơn hàng
      const orderDataResponse = await getKitchenOrders();
      // Theo hình image_ea6444.png, danh sách nằm trong .data.data
      let orderList = orderDataResponse.data || [];

      // Sắp xếp FIFO
      orderList = orderList.sort((a, b) => a.id - b.id);

      // Bước 2: Duyệt qua từng đơn hàng, gọi API lấy items tương ứng
      // Sử dụng Promise.all để fetch song song cho nhanh
      const ordersWithItemsPromises = orderList.map(async (order) => {
        // Lấy items bằng orderId của KitchenOrder
        const items = await getOrderItems(order.orderId);

        // Trả về một object mới gộp thông tin order cũ và mảng items mới
        return {
          ...order,
          items: items, // Thêm mảng items vào order object
        };
      });

      // Chờ tất cả các API call con hoàn thành
      const fullOrdersData = await Promise.all(ordersWithItemsPromises);

      setOrders(fullOrdersData);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  // Hàm xử lý cập nhật status
  const handleUpdateStatus = async (orderId, status) => {
    setUpdatingOrderId(orderId);
    try {
      await updateOrderStatus(orderId, status);

      setOrders((prevOrders) =>
        prevOrders.filter((order) => order.id !== orderId),
      );
    } catch (error) {
      console.error("Lỗi cập nhật status:", error);
      alert(error || "Không thể cập nhật trạng thái order");
    } finally {
      setUpdatingOrderId(null);
    }
  };

  useEffect(() => {
    fetchOrders();

    connectToKitchenSocket(async (newOrder) => {
      audioRef.current
        .play()
        .catch((e) => console.log("Chrome chặn autoplay:", e));

      // KHI CÓ ĐƠN MỚI TỪ SOCKET, BẠN CŨNG PHẢI ĐẢM BẢO NÓ CÓ ITEMS
      // Nếu DTO từ socket CHƯA CÓ items, bạn phải gọi API lấy items trước khi cập nhật state
      let orderToAdd = newOrder;
      if (!newOrder.items || newOrder.items.length === 0) {
        const fetchedItems = await getOrderItems(newOrder.orderId);
        orderToAdd = { ...newOrder, items: fetchedItems };
      }

      setOrders((prevOrders) => {
        const exists = prevOrders.some(
          (o) => o.id === orderToAdd.kitchenOrderId || o.id === orderToAdd.id,
        );
        if (exists) return prevOrders;

        return [...prevOrders, orderToAdd];
      });
    });

    return () => {
      disconnectSocket();
    };
  }, []);

  // Filter local
  const filteredOrders = orders.filter(
    (order) =>
      order.tableId?.toString().includes(searchTerm) ||
      order.id?.toString().includes(searchTerm),
  );

  return (
    <div className="admin-content">
      {/* ... Phần Header giữ nguyên ... */}

      <div className="page-header">
        <div className="header-left">
          <h2>Kitchen Monitor (Realtime)</h2>
          <p className="subtitle">Quản lý chế biến món ăn</p>
        </div>
        <button className="btn-refresh" onClick={fetchOrders}>
          <FaSyncAlt className={loading ? "spin" : ""} /> Sync
        </button>
      </div>

      <div className="search-section"></div>

      <div className="order-grid">
        {filteredOrders.length > 0 ? (
          filteredOrders.map((order) => (
            <div className="order-card" key={order.id || order.kitchenOrderId}>
              <div className="card-header">
                <div className="table-badge">Bàn {order.tableId}</div>
                <div className="order-meta">
                  <span className="order-id">
                    #{order.id || order.kitchenOrderId}
                  </span>
                  <span className="order-time">
                    <FaClock style={{ marginRight: 4 }} />
                    {new Date(order.createdAt).toLocaleTimeString("vi-VN", {
                      hour: "2-digit",
                      minute: "2-digit",
                    })}
                  </span>
                </div>
              </div>

              <div className="card-body">
                {/* LƯU Ý BẢO VỆ MẢNG ITEMS */}
                {order.items && order.items.length > 0 ? (
                  order.items.map((item) => (
                    <div className="order-item" key={item.id}>
                      <div className="item-info">
                        <div className="item-main">
                          <span className="qty">x{item.quantity}</span>
                          <span className="name">
                            {item.productName || item.menuItemName}
                          </span>
                        </div>
                        {item.note && (
                          <div className="item-note">Note: {item.note}</div>
                        )}
                      </div>
                      <span className="status-badge cooking">
                        {item.status || "COOKING"}
                      </span>
                    </div>
                  ))
                ) : (
                  <div className="empty-items">Đang tải món...</div>
                )}
              </div>

              {/* ... Phần Footer giữ nguyên ... */}
              <div className="card-footer">
                <button
                  className="btn-action btn-reject"
                  onClick={() => handleUpdateStatus(order.id, "CANCELLED")}
                  disabled={updatingOrderId === order.id}
                >
                  {updatingOrderId === order.id ? "Đang xử lý..." : "Hủy"}
                </button>
                <button
                  className="btn-action btn-done"
                  onClick={() => handleUpdateStatus(order.id, "DONE")}
                  disabled={updatingOrderId === order.id}
                >
                  {updatingOrderId === order.id ? "Đang xử lý..." : "Xong"}
                </button>
              </div>
            </div>
          ))
        ) : (
          <div className="empty-state">Bếp đang rảnh...</div>
        )}
      </div>
    </div>
  );
};

export default KitchenOrder;
