import axiosClient from "../../utils/api";
export const createOrder = async (cartItems) => {
  try {
    const payload = {
      items: cartItems.map((item) => ({
        productId: item.id,
        quantity: item.quantity,
        note: item.note || "",
      })),
    };

    const response = await axiosClient.post(`/api/v1/orders`, payload);
    return response.data;
  } catch (error) {
    throw error.response?.data?.message || "Đặt món thất bại";
  }
};
export const getTableOrderDetails = async (tableId) => {
  try {
    const response = await axiosClient.get(`/api/v1/orders/table/${tableId}`);
    return response.data.data;
  } catch (error) {
    throw error.response?.data?.message || "Lỗi lấy chi tiết order của bàn";
  }
};

export const updateOrderItemQuantity = async (itemId, quantity) => {
  try {
    const response = await axiosClient.put(`/api/v1/orders/items/${itemId}`, {
      quantity,
    });
    return response.data;
  } catch (error) {
    throw error.response?.data?.message || "Lỗi cập nhật số lượng món";
  }
};

export const deleteOrderItem = async (itemId) => {
  try {
    const response = await axiosClient.delete(`/api/v1/orders/items/${itemId}`);
    return response.data;
  } catch (error) {
    throw error.response?.data?.message || "Lỗi xóa món ăn";
  }
};
export const getOrders = async (params) => {
  let url = `/api/v1/orders`;
  const configParams = { ...params };

  if (configParams.timeFilter && configParams.timeFilter !== "ALL_TIME") {
    url = `/api/v1/orders/filter-time`;

    configParams.filter = configParams.timeFilter;
    delete configParams.timeFilter;
  } else {
    delete configParams.timeFilter;
  }

  try {
    const response = await axiosClient.get(url, {
      params: configParams,
    });
    return response.data;
  } catch (error) {
    throw error.response?.data?.message || "Lỗi tải danh sách order";
  }
};

// Lấy thống kê dashboard (Total, Completed, Cancelled...)
export const getDashboardSummary = async () => {
  try {
    const response = await axiosClient.get(`/api/v1/orders/dashboard/summary`);
    return response.data;
  } catch (error) {
    console.error("Lỗi tải thống kê:", error);
    return null;
  }
};

// Cập nhật trạng thái (Dùng khi Admin muốn cancel hoặc hoàn tất thủ công)
export const updateOrderStatus = async (id, status) => {
  try {
    await axiosClient.put(`/api/v1/orders/${id}/status`, null, {
      params: { status },
    });
  } catch (error) {
    throw error.response?.data?.message || "Lỗi cập nhật trạng thái";
  }
};
