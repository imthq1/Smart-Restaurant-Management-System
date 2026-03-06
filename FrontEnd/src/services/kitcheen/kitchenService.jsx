import axiosClient from "../../utils/api";

export const getKitchenOrders = async () => {
  try {
    const response = await axiosClient.get(`/api/v1/kitchens/orders`);
    return response.data;
  } catch (error) {
    throw error.response?.data?.message || "Lỗi tải danh sách order";
  }
};
export const getOrderItems = async (orderId) => {
  try {
    const response = await axiosClient.get(`/api/v1/orders/${orderId}/items`);

    return response.data.data;
  } catch (error) {
    console.error(`Lỗi tải items cho order ${orderId}:`, error);
    return [];
  }
};
export const updateOrderStatus = async (orderId, status) => {
  try {
    const response = await axiosClient.put(
      `/api/v1/kitchens/setStatusOrder/${orderId}`,
      status,
      {
        headers: {
          "Content-Type": "application/json",
        },
      },
    );
    return response.data.data;
  } catch (error) {
    throw error.response?.data?.data.message || "Lỗi cập nhật trạng thái order";
  }
};
