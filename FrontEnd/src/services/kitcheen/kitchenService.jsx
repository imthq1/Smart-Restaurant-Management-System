import axiosClient from "../../utils/api";

export const getKitchenOrders = async () => {
  try {
    const response = await axiosClient.get(`/api/v1/kitchens/orders`);
    return response.data;
  } catch (error) {
    throw error.response?.data?.message || "Lỗi tải danh sách order";
  }
};

export const updateOrderStatus = async (kitchenOrderId, status) => {
  try {
    const response = await axiosClient.put(
      `/api/v1/kitchens/setStatusOrder/${kitchenOrderId}`,
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
